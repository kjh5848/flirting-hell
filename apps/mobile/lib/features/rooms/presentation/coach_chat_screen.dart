import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../data/remote/rooms_api.dart';

/// 상담방 안에서 코치와 나누는 대화. 메시지는 세션 동안만 클라이언트에 유지하고
/// 서버에 원문을 저장하지 않는다(메모리 설계 원칙).
class CoachChatScreen extends ConsumerStatefulWidget {
  const CoachChatScreen({required this.roomId, super.key});

  final String roomId;

  @override
  ConsumerState<CoachChatScreen> createState() => _CoachChatScreenState();
}

class _CoachChatScreenState extends ConsumerState<CoachChatScreen> {
  final _controller = TextEditingController();
  final _scrollController = ScrollController();
  final List<_Msg> _messages = [];
  bool _sending = false;

  @override
  void dispose() {
    _controller.dispose();
    _scrollController.dispose();
    super.dispose();
  }

  Future<void> _send() async {
    final text = _controller.text.trim();
    if (text.isEmpty || _sending) return;

    final history = [
      for (final m in _messages)
        {'role': m.isUser ? 'USER' : 'COACH', 'text': m.text},
    ];
    setState(() {
      _messages.add(_Msg(text: text, isUser: true));
      _sending = true;
      _controller.clear();
    });
    _scrollToBottom();

    try {
      final reply =
          await ref.read(roomsApiProvider).coachReply(widget.roomId, history, text);
      if (mounted) {
        setState(() => _messages.add(_Msg(text: reply, isUser: false)));
      }
    } catch (_) {
      if (mounted) {
        setState(() => _messages.add(const _Msg(
              text: '지금은 답하기 어려워요. 잠시 후 다시 시도해 주세요.',
              isUser: false,
            )));
      }
    } finally {
      if (mounted) setState(() => _sending = false);
      _scrollToBottom();
    }
  }

  void _scrollToBottom() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (_scrollController.hasClients) {
        _scrollController.animateTo(
          _scrollController.position.maxScrollExtent,
          duration: const Duration(milliseconds: 200),
          curve: Curves.easeOut,
        );
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('코치와 대화')),
      body: SafeArea(
        child: Column(
          children: [
            Expanded(
              child: _messages.isEmpty
                  ? const _EmptyCoachState()
                  : ListView.builder(
                      controller: _scrollController,
                      padding: const EdgeInsets.fromLTRB(20, 16, 20, 16),
                      itemCount: _messages.length,
                      itemBuilder: (context, index) =>
                          _Bubble(message: _messages[index]),
                    ),
            ),
            if (_sending)
              const Padding(
                padding: EdgeInsets.only(bottom: 8),
                child: Text('코치가 생각 중…',
                    style: TextStyle(color: Color(0xFF9B8A8E), fontSize: 12)),
              ),
            _Composer(
              controller: _controller,
              enabled: !_sending,
              onSend: _send,
            ),
          ],
        ),
      ),
    );
  }
}

class _EmptyCoachState extends StatelessWidget {
  const _EmptyCoachState();

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Text(
          '지금 그 사람과의 상황, 편하게 털어놓아 보세요.\n같이 정리하고 다음 한마디를 찾아드려요.',
          textAlign: TextAlign.center,
          style: Theme.of(context).textTheme.bodyMedium,
        ),
      ),
    );
  }
}

class _Bubble extends StatelessWidget {
  const _Bubble({required this.message});

  final _Msg message;

  @override
  Widget build(BuildContext context) {
    final isUser = message.isUser;
    return Align(
      alignment: isUser ? Alignment.centerRight : Alignment.centerLeft,
      child: Container(
        margin: const EdgeInsets.only(bottom: 10),
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
        constraints: BoxConstraints(
          maxWidth: MediaQuery.of(context).size.width * 0.78,
        ),
        decoration: BoxDecoration(
          color: isUser ? const Color(0xFFE43F5A) : Colors.white,
          borderRadius: BorderRadius.circular(18),
          border: isUser ? null : Border.all(color: const Color(0xFFEDE3DF)),
        ),
        child: Text(
          message.text,
          style: TextStyle(
            color: isUser ? Colors.white : const Color(0xFF1D1719),
            fontSize: 14,
            height: 1.4,
            fontWeight: FontWeight.w600,
          ),
        ),
      ),
    );
  }
}

class _Composer extends StatelessWidget {
  const _Composer({
    required this.controller,
    required this.enabled,
    required this.onSend,
  });

  final TextEditingController controller;
  final bool enabled;
  final VoidCallback onSend;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 4, 16, 12),
      child: Row(
        children: [
          Expanded(
            child: TextField(
              controller: controller,
              enabled: enabled,
              minLines: 1,
              maxLines: 4,
              textInputAction: TextInputAction.send,
              onSubmitted: (_) => onSend(),
              decoration: const InputDecoration(
                hintText: '고민을 편하게 적어보세요',
              ),
            ),
          ),
          const SizedBox(width: 8),
          IconButton.filled(
            onPressed: enabled ? onSend : null,
            icon: const Icon(Icons.arrow_upward_rounded),
          ),
        ],
      ),
    );
  }
}

class _Msg {
  const _Msg({required this.text, required this.isUser});

  final String text;
  final bool isUser;
}
