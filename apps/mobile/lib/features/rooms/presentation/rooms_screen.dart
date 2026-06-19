import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/widgets/app_status_chip.dart';
import '../../../core/widgets/compact_list_tile_card.dart';
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
        SectionCard(
          radius: 24,
          backgroundColor: const Color(0xFFFFF8F4),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const AppStatusChip(label: '상대별 보관'),
              const SizedBox(height: 10),
              Text('한 사람당 하나의 상담방',
                  style: Theme.of(context).textTheme.titleLarge),
              const SizedBox(height: 8),
              Text(
                '카톡, DM, 문자, 상황 설명을 상담방별로 붙여넣고 결과만 요약해서 남깁니다.',
                style: Theme.of(context).textTheme.bodyMedium,
              ),
            ],
          ),
        ),
        const SizedBox(height: 14),
        FilledButton(
          onPressed: () => _showCreateRoomSheet(context, ref),
          child: const Text('새 상담방 만들기'),
        ),
        const SizedBox(height: 18),
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
    return CompactListTileCard(
      title: room.alias,
      subtitle: room.lastTurnSummary.isEmpty
          ? room.currentConcern ?? '아직 고민이 입력되지 않았어요'
          : room.lastTurnSummary,
      trailingLabel: _relationshipStageLabel(room.relationshipStage),
      trailingTone: AppStatusChipTone.accent,
      onTap: () => context.go('/rooms/${room.roomId}'),
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
    backgroundColor: Colors.transparent,
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
  String _concernPreset = _customPresetValue;
  String _cautionPreset = _customPresetValue;
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
        child: Form(
          key: _formKey,
          child: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Center(
                  child: DecoratedBox(
                    decoration: BoxDecoration(
                      color: const Color(0xFFEFE0E3),
                      borderRadius: BorderRadius.circular(999),
                    ),
                    child: const SizedBox(width: 86, height: 4),
                  ),
                ),
                const SizedBox(height: 24),
                Text('새 상담방 만들기',
                    style: Theme.of(context).textTheme.titleLarge),
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
                    DropdownMenuItem(
                        value: 'FIRST_CONTACT', child: Text('처음 연락')),
                    DropdownMenuItem(value: 'TALKING', child: Text('썸')),
                    DropdownMenuItem(
                        value: 'BEFORE_DATE', child: Text('데이트 전')),
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
                _PresetTextFormField(
                  label: '현재 고민',
                  helperText: '예시를 고른 뒤 내 상황에 맞게 수정할 수 있어요.',
                  value: _concernPreset,
                  presets: _concernPresets,
                  controller: _concernController,
                  onChanged: (value) => setState(() => _concernPreset = value),
                ),
                const SizedBox(height: 8),
                _PresetTextFormField(
                  label: '조심할 점',
                  helperText: '부담, 재촉, 너무 빠른 고백처럼 피하고 싶은 방향을 적어요.',
                  value: _cautionPreset,
                  presets: _cautionPresets,
                  controller: _cautionController,
                  onChanged: (value) => setState(() => _cautionPreset = value),
                ),
                DropdownButtonFormField<String>(
                  value: _preferredStrategyId,
                  decoration: const InputDecoration(labelText: '기본 전략'),
                  items: const [
                    DropdownMenuItem(
                        value: 'DEVELOP_ROMANCE', child: Text('연애로 발전')),
                    DropdownMenuItem(
                        value: 'CHECK_RELATIONSHIP_STATUS',
                        child: Text('여친/남친 여부 확인')),
                    DropdownMenuItem(value: 'MAKE_PLAN', child: Text('약속 잡기')),
                    DropdownMenuItem(
                        value: 'MARRIAGE_VALUES', child: Text('결혼 가치관')),
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

const _customPresetValue = '__custom__';

const _concernPresets = [
  _TextPreset(
    value: 'interest',
    label: '상대 마음이 궁금해요',
    text: '상대가 나에게 호감이 있는지 알고 싶어요.',
  ),
  _TextPreset(
    value: 'continue',
    label: '대화를 이어가고 싶어요',
    text: '대화가 끊기지 않게 자연스럽게 이어가고 싶어요.',
  ),
  _TextPreset(
    value: 'plan',
    label: '약속을 잡고 싶어요',
    text: '부담스럽지 않게 다음 약속을 잡고 싶어요.',
  ),
  _TextPreset(
    value: 'confession',
    label: '고백 타이밍이 고민돼요',
    text: '지금 고백해도 괜찮은 타이밍인지 알고 싶어요.',
  ),
  _TextPreset(
    value: 'late_reply',
    label: '답장이 늦어서 고민이에요',
    text: '상대 답장이 늦을 때 어떻게 반응해야 할지 모르겠어요.',
  ),
];

const _cautionPresets = [
  _TextPreset(
    value: 'not_pressure',
    label: '부담스럽게 보이고 싶지 않아요',
    text: '상대에게 부담스럽거나 집착처럼 보이는 말은 피하고 싶어요.',
  ),
  _TextPreset(
    value: 'not_rush',
    label: '너무 빠르게 가고 싶지 않아요',
    text: '관계를 너무 빠르게 몰아가는 느낌은 피하고 싶어요.',
  ),
  _TextPreset(
    value: 'not_pushy',
    label: '재촉하고 싶지 않아요',
    text: '상대가 바쁠 수 있어서 답장을 재촉하는 표현은 피하고 싶어요.',
  ),
  _TextPreset(
    value: 'not_light',
    label: '가벼워 보이고 싶지 않아요',
    text: '진심은 전하되 너무 가볍거나 장난스럽게 보이고 싶지 않아요.',
  ),
  _TextPreset(
    value: 'recover',
    label: '이전 실수를 회복하고 싶어요',
    text: '이전 대화에서 어색했던 표현을 자연스럽게 회복하고 싶어요.',
  ),
];

class _TextPreset {
  const _TextPreset({
    required this.value,
    required this.label,
    required this.text,
  });

  final String value;
  final String label;
  final String text;
}

class _PresetTextFormField extends StatelessWidget {
  const _PresetTextFormField({
    required this.label,
    required this.helperText,
    required this.value,
    required this.presets,
    required this.controller,
    required this.onChanged,
  });

  final String label;
  final String helperText;
  final String value;
  final List<_TextPreset> presets;
  final TextEditingController controller;
  final ValueChanged<String> onChanged;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        DropdownButtonFormField<String>(
          value: value,
          decoration: InputDecoration(labelText: '$label 예시'),
          items: [
            const DropdownMenuItem(
              value: _customPresetValue,
              child: Text('직접 입력'),
            ),
            for (final preset in presets)
              DropdownMenuItem(
                value: preset.value,
                child: Text(preset.label),
              ),
          ],
          onChanged: (selectedValue) {
            if (selectedValue == null) {
              return;
            }

            final preset = _findPreset(selectedValue, presets);
            if (preset != null) {
              controller.text = preset.text;
              controller.selection =
                  TextSelection.collapsed(offset: controller.text.length);
            } else if (_isPresetText(controller.text, presets)) {
              controller.clear();
            }

            onChanged(selectedValue);
          },
        ),
        TextFormField(
          controller: controller,
          decoration: InputDecoration(
            labelText: label,
            helperText: helperText,
          ),
          maxLength: 160,
        ),
      ],
    );
  }
}

bool _isPresetText(String text, List<_TextPreset> presets) {
  return presets.any((preset) => preset.text == text);
}

_TextPreset? _findPreset(String value, List<_TextPreset> presets) {
  for (final preset in presets) {
    if (preset.value == value) {
      return preset;
    }
  }
  return null;
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
