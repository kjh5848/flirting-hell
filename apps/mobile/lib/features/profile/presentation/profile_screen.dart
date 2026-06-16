import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/widgets/app_status_chip.dart';
import '../../../core/widgets/compact_list_tile_card.dart';
import '../../../core/widgets/screen_frame.dart';
import '../../../data/models/bootstrap_snapshot.dart';
import '../../../data/remote/bootstrap_api.dart';
import '../../auth/application/auth_controller.dart';
import '../../home/application/bootstrap_provider.dart';

class ProfileScreen extends ConsumerWidget {
  const ProfileScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final bootstrap = ref.watch(bootstrapProvider).valueOrNull;
    final profile = bootstrap?.user.profile;

    return ScreenFrame(
      title: '내 정보',
      subtitle: '내 말투, 연애 스타일, 조언 수위는 전체 상담방에 기본값으로 적용합니다.',
      children: [
        DecoratedBox(
          decoration: BoxDecoration(
            color: const Color(0xFF1D1719),
            borderRadius: BorderRadius.circular(26),
          ),
          child: Padding(
            padding: const EdgeInsets.all(20),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const AppStatusChip(label: '전역 설정'),
                const SizedBox(height: 12),
                Text(
                  profile?.nickname ?? '별칭 없음',
                  style: const TextStyle(
                    color: Colors.white,
                    fontSize: 26,
                    fontWeight: FontWeight.w900,
                    height: 1.1,
                    letterSpacing: -0.9,
                  ),
                ),
                const SizedBox(height: 8),
                Text(
                  '답장 추천은 이 기준을 기본값으로 사용합니다.',
                  style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                        color: const Color(0xFFF6EDEE),
                      ),
                ),
              ],
            ),
          ),
        ),
        const SizedBox(height: 12),
        _ProfileSetting(
          title: '내 말투',
          value: profile?.speechStyle ?? '-',
          editable: profile != null,
          onEdit: profile == null
              ? null
              : () => _editField(context, ref, profile, _ProfileField.speechStyle),
        ),
        const SizedBox(height: 10),
        _ProfileSetting(
          title: '연애 스타일',
          value: profile?.datingStyle ?? '-',
          editable: profile != null,
          onEdit: profile == null
              ? null
              : () => _editField(context, ref, profile, _ProfileField.datingStyle),
        ),
        const SizedBox(height: 10),
        _ProfileSetting(
          title: '조언 수위',
          value: _guidanceLabel(profile?.guidanceLevel ?? 'BALANCED'),
          editable: profile != null,
          onEdit: profile == null
              ? null
              : () =>
                  _editField(context, ref, profile, _ProfileField.guidanceLevel),
        ),
        const SizedBox(height: 10),
        _ProfileSetting(
          title: '원하는 상대 스타일',
          value: profile?.preferredPartnerStyle ?? '아직 설정하지 않았어요',
          editable: profile != null,
          onEdit: profile == null
              ? null
              : () => _editField(
                  context, ref, profile, _ProfileField.preferredPartnerStyle),
        ),
        const SizedBox(height: 10),
        _ProfileSetting(
          title: '피하고 싶은 조언',
          value: profile?.avoidAdvice ?? '단정하거나 압박하는 조언은 피하기',
          editable: profile != null,
          onEdit: profile == null
              ? null
              : () => _editField(context, ref, profile, _ProfileField.avoidAdvice),
        ),
        const SizedBox(height: 18),
        CompactListTileCard(
          leadingText: '향',
          title: '연애 성향',
          subtitle: '내 성향과 이상형을 설정해 상대와의 적합도를 봅니다',
          onTap: () => context.go('/personality'),
        ),
        const SizedBox(height: 10),
        CompactListTileCard(
          leadingText: '권',
          title: '분석권',
          subtitle: bootstrap == null
              ? '무료 분석과 보유 분석권을 확인합니다'
              : '오늘 무료 ${bootstrap.usage.freeRemaining}회 · 보유 ${bootstrap.usage.creditBalance}회',
          onTap: () => context.go('/billing'),
        ),
        const SizedBox(height: 18),
        OutlinedButton(
          onPressed: () => ref.read(authControllerProvider).signOut(),
          child: const Text('로그아웃'),
        ),
      ],
    );
  }
}

Future<void> _editField(
  BuildContext context,
  WidgetRef ref,
  BootstrapProfile current,
  _ProfileField field,
) async {
  final result = await showModalBottomSheet<String>(
    context: context,
    isScrollControlled: true,
    backgroundColor: Colors.transparent,
    builder: (context) => _ProfileEditSheet(field: field, current: current),
  );

  if (result == null || !context.mounted) {
    return;
  }

  // Build the payload from the *current* profile so the PATCH never drops the
  // fields the user did not touch, then override only the edited one.
  final payload = UpdateProfilePayload(
    nickname: current.nickname,
    speechStyle:
        field == _ProfileField.speechStyle ? result : current.speechStyle,
    datingStyle:
        field == _ProfileField.datingStyle ? result : current.datingStyle,
    guidanceLevel:
        field == _ProfileField.guidanceLevel ? result : current.guidanceLevel,
    preferredPartnerStyle: field == _ProfileField.preferredPartnerStyle
        ? _blankToNull(result)
        : current.preferredPartnerStyle,
    avoidAdvice: field == _ProfileField.avoidAdvice
        ? _blankToFallback(result, current.avoidAdvice)
        : current.avoidAdvice,
  );

  try {
    await ref.read(bootstrapApiProvider).updateProfile(payload);
    ref.invalidate(bootstrapProvider);
  } catch (_) {
    if (context.mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('내 정보를 저장하지 못했습니다.')),
      );
    }
  }
}

enum _ProfileField {
  speechStyle,
  datingStyle,
  guidanceLevel,
  preferredPartnerStyle,
  avoidAdvice,
}

class _ProfileEditSheet extends StatefulWidget {
  const _ProfileEditSheet({
    required this.field,
    required this.current,
  });

  final _ProfileField field;
  final BootstrapProfile current;

  @override
  State<_ProfileEditSheet> createState() => _ProfileEditSheetState();
}

class _ProfileEditSheetState extends State<_ProfileEditSheet> {
  late String _choice;
  late final TextEditingController _textController;

  @override
  void initState() {
    super.initState();
    _choice = _initialChoice();
    _textController = TextEditingController(text: _initialText());
  }

  String _initialChoice() {
    return switch (widget.field) {
      _ProfileField.speechStyle => widget.current.speechStyle,
      _ProfileField.datingStyle => widget.current.datingStyle,
      _ProfileField.guidanceLevel => widget.current.guidanceLevel,
      _ => '',
    };
  }

  String _initialText() {
    return switch (widget.field) {
      _ProfileField.preferredPartnerStyle =>
        widget.current.preferredPartnerStyle ?? '',
      _ProfileField.avoidAdvice => widget.current.avoidAdvice,
      _ => '',
    };
  }

  @override
  void dispose() {
    _textController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return DecoratedBox(
      decoration: const BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.vertical(top: Radius.circular(34)),
      ),
      child: Padding(
        padding: EdgeInsets.only(
          left: 20,
          right: 20,
          top: 12,
          bottom: MediaQuery.of(context).viewInsets.bottom + 20,
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Center(
              child: DecoratedBox(
                decoration: BoxDecoration(
                  color: const Color(0xFFEDE3DF),
                  borderRadius: BorderRadius.circular(999),
                ),
                child: const SizedBox(width: 86, height: 4),
              ),
            ),
            const SizedBox(height: 24),
            Text(_fieldTitle, style: Theme.of(context).textTheme.titleLarge),
            const SizedBox(height: 16),
            ..._editor(context),
            const SizedBox(height: 18),
            FilledButton(
              onPressed: () => Navigator.of(context).pop(_resultValue()),
              child: const Text('저장'),
            ),
          ],
        ),
      ),
    );
  }

  String get _fieldTitle => switch (widget.field) {
        _ProfileField.speechStyle => '내 말투',
        _ProfileField.datingStyle => '연애 스타일',
        _ProfileField.guidanceLevel => '조언 수위',
        _ProfileField.preferredPartnerStyle => '원하는 상대 스타일',
        _ProfileField.avoidAdvice => '피하고 싶은 조언',
      };

  String _resultValue() {
    return switch (widget.field) {
      _ProfileField.speechStyle ||
      _ProfileField.datingStyle ||
      _ProfileField.guidanceLevel =>
        _choice,
      _ => _textController.text.trim(),
    };
  }

  List<Widget> _editor(BuildContext context) {
    switch (widget.field) {
      case _ProfileField.speechStyle:
        return [_choiceList(_speechStyleOptions)];
      case _ProfileField.datingStyle:
        return [_choiceList(_datingStyleOptions)];
      case _ProfileField.guidanceLevel:
        return [
          SegmentedButton<String>(
            segments: const [
              ButtonSegment(value: 'SUPPORTIVE', label: Text('부드럽게')),
              ButtonSegment(value: 'BALANCED', label: Text('균형')),
              ButtonSegment(value: 'REALITY_CHECK', label: Text('현실 체크')),
            ],
            selected: {_choice},
            onSelectionChanged: (values) =>
                setState(() => _choice = values.first),
          ),
        ];
      case _ProfileField.preferredPartnerStyle:
        return [
          TextField(
            controller: _textController,
            maxLength: 160,
            decoration: const InputDecoration(
              labelText: '원하는 상대 스타일',
              helperText: '예: 다정하게 표현하는 사람, 대화를 이어주는 사람',
            ),
          ),
        ];
      case _ProfileField.avoidAdvice:
        return [
          TextField(
            controller: _textController,
            maxLength: 160,
            decoration: const InputDecoration(
              labelText: '피하고 싶은 조언',
              helperText: '예: 단정하지 않기, 재촉하지 않기',
            ),
          ),
        ];
    }
  }

  Widget _choiceList(List<String> options) {
    return Column(
      children: [
        for (final option in options)
          RadioListTile<String>(
            value: option,
            groupValue: _choice,
            onChanged: (value) => setState(() => _choice = value ?? _choice),
            title: Text(
              option,
              style: Theme.of(context).textTheme.titleMedium,
            ),
            contentPadding: EdgeInsets.zero,
            activeColor: Theme.of(context).colorScheme.primary,
          ),
      ],
    );
  }
}

const _speechStyleOptions = [
  '짧고 자연스럽게',
  '다정하고 부드럽게',
  '장난스럽고 가볍게',
  '차분하고 예의 있게',
];

const _datingStyleOptions = [
  '천천히 확인하면서 대화 이어가기',
  '설레는 분위기를 자연스럽게 만들기',
  '약속으로 이어지는 흐름 만들기',
  '상대 속도에 맞추기',
];

class _ProfileSetting extends StatelessWidget {
  const _ProfileSetting({
    required this.title,
    required this.value,
    this.editable = false,
    this.onEdit,
  });

  final String title;
  final String value;
  final bool editable;
  final VoidCallback? onEdit;

  @override
  Widget build(BuildContext context) {
    return CompactListTileCard(
      title: title,
      subtitle: value,
      trailingLabel: editable ? '수정' : null,
      trailingTone: AppStatusChipTone.neutral,
      showChevron: false,
      onTap: onEdit,
    );
  }
}

String _guidanceLabel(String value) {
  return switch (value) {
    'SUPPORTIVE' => '부드럽게',
    'REALITY_CHECK' => '현실 체크',
    _ => '균형 조언',
  };
}

String? _blankToNull(String value) {
  final trimmed = value.trim();
  return trimmed.isEmpty ? null : trimmed;
}

String _blankToFallback(String value, String fallback) {
  final trimmed = value.trim();
  return trimmed.isEmpty ? fallback : trimmed;
}
