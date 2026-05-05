import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/widgets/screen_frame.dart';
import '../../../core/widgets/section_card.dart';
import '../../../data/models/room_models.dart';
import '../../../data/remote/rooms_api.dart';
import '../../home/application/bootstrap_provider.dart';
import '../application/rooms_provider.dart';

class RoomsScreen extends ConsumerWidget {
  const RoomsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final roomsAsync = ref.watch(roomsProvider);

    return ScreenFrame(
      title: '상담방',
      subtitle: '상대별로 붙여넣은 대화, 상황 요약, 추천 답장을 분리해서 보관합니다.',
      children: [
        FilledButton.icon(
          onPressed: () => _showCreateRoomSheet(context, ref),
          icon: const Icon(Icons.add_rounded),
          label: const Text('새 상담방 만들기'),
        ),
        const SizedBox(height: 16),
        roomsAsync.when(
          data: (roomList) {
            if (roomList.rooms.isEmpty) {
              return const _EmptyRoomsCard();
            }

            return Column(
              children: [
                for (final room in roomList.rooms) ...[
                  _RoomCard(room: room),
                  const SizedBox(height: 10),
                ],
              ],
            );
          },
          error: (error, stackTrace) => const _StateCard(
            title: '상담방을 불러오지 못했어요',
            body: '서버 실행 상태와 로그인 토큰을 확인해야 합니다.',
          ),
          loading: () => const SectionCard(
            child: Center(child: CircularProgressIndicator()),
          ),
        ),
      ],
    );
  }
}

class _RoomCard extends StatelessWidget {
  const _RoomCard({
    required this.room,
  });

  final RoomSummary room;

  @override
  Widget build(BuildContext context) {
    return InkWell(
      borderRadius: BorderRadius.circular(24),
      onTap: () => context.go('/rooms/${room.roomId}'),
      child: SectionCard(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                CircleAvatar(
                  backgroundColor: const Color(0xFFFFF1F2),
                  foregroundColor: Theme.of(context).colorScheme.primary,
                  child: Text(room.alias.characters.first),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Text(
                    room.alias,
                    style: Theme.of(context).textTheme.titleLarge,
                  ),
                ),
                Text(
                  _relationshipStageLabel(room.relationshipStage),
                  style: Theme.of(context).textTheme.bodyMedium,
                ),
              ],
            ),
            const SizedBox(height: 14),
            Text(
              room.currentConcern ?? '아직 고민이 입력되지 않았어요',
              style: Theme.of(context).textTheme.titleMedium,
            ),
            const SizedBox(height: 8),
            Text(room.lastTurnSummary, style: Theme.of(context).textTheme.bodyMedium),
          ],
        ),
      ),
    );
  }
}

class _EmptyRoomsCard extends StatelessWidget {
  const _EmptyRoomsCard();

  @override
  Widget build(BuildContext context) {
    return const _StateCard(
      title: '아직 상담방이 없어요',
      body: '상대 별칭, 관계 단계, 지금 고민을 적어 첫 상담방을 만드세요.',
    );
  }
}

class _StateCard extends StatelessWidget {
  const _StateCard({
    required this.title,
    required this.body,
  });

  final String title;
  final String body;

  @override
  Widget build(BuildContext context) {
    return SectionCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(title, style: Theme.of(context).textTheme.titleMedium),
          const SizedBox(height: 8),
          Text(body, style: Theme.of(context).textTheme.bodyMedium),
        ],
      ),
    );
  }
}

Future<void> _showCreateRoomSheet(BuildContext context, WidgetRef ref) async {
  final createdRoom = await showModalBottomSheet<Room>(
    context: context,
    isScrollControlled: true,
    builder: (context) => const _CreateRoomSheet(),
  );

  if (createdRoom == null || !context.mounted) {
    return;
  }

  ref.invalidate(roomsProvider);
  ref.invalidate(bootstrapProvider);
  context.go('/rooms/${createdRoom.roomId}');
}

class _CreateRoomSheet extends ConsumerStatefulWidget {
  const _CreateRoomSheet();

  @override
  ConsumerState<_CreateRoomSheet> createState() => _CreateRoomSheetState();
}

class _CreateRoomSheetState extends ConsumerState<_CreateRoomSheet> {
  final _formKey = GlobalKey<FormState>();
  final _aliasController = TextEditingController();
  final _concernController = TextEditingController();
  final _cautionController = TextEditingController();

  String _relationshipStage = 'TALKING';
  String _preferredStrategyId = 'DEVELOP_ROMANCE';
  bool _isSubmitting = false;

  @override
  void dispose() {
    _aliasController.dispose();
    _concernController.dispose();
    _cautionController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: EdgeInsets.only(
        left: 20,
        right: 20,
        top: 20,
        bottom: MediaQuery.of(context).viewInsets.bottom + 20,
      ),
      child: Form(
        key: _formKey,
        child: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('새 상담방 만들기', style: Theme.of(context).textTheme.titleLarge),
              const SizedBox(height: 8),
              Text(
                '실명보다 별칭을 쓰고, 지금 고민과 조심할 점만 가볍게 적습니다.',
                style: Theme.of(context).textTheme.bodyMedium,
              ),
              const SizedBox(height: 18),
              TextFormField(
                controller: _aliasController,
                decoration: const InputDecoration(labelText: '상대 별칭'),
                maxLength: 40,
                validator: (value) {
                  if (value == null || value.trim().isEmpty) {
                    return '상대 별칭을 입력하세요.';
                  }
                  return null;
                },
              ),
              DropdownButtonFormField<String>(
                value: _relationshipStage,
                decoration: const InputDecoration(labelText: '관계 단계'),
                items: const [
                  DropdownMenuItem(value: 'FIRST_CONTACT', child: Text('처음 연락')),
                  DropdownMenuItem(value: 'TALKING', child: Text('썸')),
                  DropdownMenuItem(value: 'BEFORE_DATE', child: Text('데이트 전')),
                  DropdownMenuItem(value: 'AFTER_DATE', child: Text('데이트 후')),
                  DropdownMenuItem(value: 'DATING', child: Text('연애 중')),
                  DropdownMenuItem(value: 'RECOVERY', child: Text('관계 회복')),
                  DropdownMenuItem(value: 'UNKNOWN', child: Text('아직 모르겠음')),
                ],
                onChanged: (value) {
                  if (value != null) {
                    setState(() => _relationshipStage = value);
                  }
                },
              ),
              TextFormField(
                controller: _concernController,
                decoration: const InputDecoration(labelText: '현재 고민'),
                maxLength: 160,
              ),
              TextFormField(
                controller: _cautionController,
                decoration: const InputDecoration(labelText: '조심할 점'),
                maxLength: 160,
              ),
              DropdownButtonFormField<String>(
                value: _preferredStrategyId,
                decoration: const InputDecoration(labelText: '기본 전략'),
                items: const [
                  DropdownMenuItem(value: 'DEVELOP_ROMANCE', child: Text('연애로 발전')),
                  DropdownMenuItem(value: 'CHECK_RELATIONSHIP_STATUS', child: Text('여친/남친 여부 확인')),
                  DropdownMenuItem(value: 'MAKE_PLAN', child: Text('약속 잡기')),
                  DropdownMenuItem(value: 'MARRIAGE_VALUES', child: Text('결혼 가치관')),
                  DropdownMenuItem(value: 'SLOW_DOWN', child: Text('속도 조절')),
                ],
                onChanged: (value) {
                  if (value != null) {
                    setState(() => _preferredStrategyId = value);
                  }
                },
              ),
              const SizedBox(height: 18),
              FilledButton(
                onPressed: _isSubmitting ? null : _submit,
                child: Text(_isSubmitting ? '만드는 중' : '상담방 만들기'),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) {
      return;
    }

    setState(() => _isSubmitting = true);
    try {
      final room = await ref.read(roomsApiProvider).createRoom(
            CreateRoomPayload(
              alias: _aliasController.text.trim(),
              relationshipStage: _relationshipStage,
              currentConcern: _blankToNull(_concernController.text),
              cautionNotes: _blankToNull(_cautionController.text),
              preferredStrategyId: _preferredStrategyId,
            ),
          );
      if (mounted) {
        Navigator.of(context).pop(room);
      }
    } catch (_) {
      if (mounted) {
        setState(() => _isSubmitting = false);
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('상담방을 만들지 못했습니다.')),
        );
      }
    }
  }
}

String? _blankToNull(String value) {
  final trimmed = value.trim();
  return trimmed.isEmpty ? null : trimmed;
}

String _relationshipStageLabel(String value) {
  return switch (value) {
    'FIRST_CONTACT' => '처음 연락',
    'TALKING' => '썸',
    'BEFORE_DATE' => '데이트 전',
    'AFTER_DATE' => '데이트 후',
    'DATING' => '연애 중',
    'RECOVERY' => '관계 회복',
    _ => '아직 모르겠음',
  };
}
