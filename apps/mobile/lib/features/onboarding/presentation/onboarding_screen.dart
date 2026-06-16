import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/widgets/app_status_chip.dart';
import '../../../core/widgets/section_card.dart';
import '../../../data/remote/bootstrap_api.dart';
import '../../home/application/bootstrap_provider.dart';

class OnboardingScreen extends ConsumerStatefulWidget {
  const OnboardingScreen({super.key});

  @override
  ConsumerState<OnboardingScreen> createState() => _OnboardingScreenState();
}

class _OnboardingScreenState extends ConsumerState<OnboardingScreen> {
  final _formKey = GlobalKey<FormState>();
  final _nicknameController = TextEditingController();
  final _preferredPartnerStyleController = TextEditingController();
  final _avoidAdviceController = TextEditingController(
    text: '단정하거나 압박하는 조언은 피하기',
  );

  String _speechStyle = '짧고 자연스럽게';
  String _datingStyle = '천천히 확인하면서 대화 이어가기';
  String _guidanceLevel = 'BALANCED';
  bool _isSubmitting = false;
  bool _didRouteCompletedUser = false;

  @override
  void dispose() {
    _nicknameController.dispose();
    _preferredPartnerStyleController.dispose();
    _avoidAdviceController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final bootstrapAsync = ref.watch(bootstrapProvider);
    final profile = bootstrapAsync.valueOrNull?.user.profile;

    if (profile != null) {
      _setControllerTextIfEmpty(_nicknameController, profile.nickname);
      _setControllerTextIfEmpty(
        _preferredPartnerStyleController,
        profile.preferredPartnerStyle,
      );
      _setControllerTextIfEmpty(_avoidAdviceController, profile.avoidAdvice);
    }

    final onboardingCompleted =
        bootstrapAsync.valueOrNull?.user.onboardingCompleted ?? false;
    if (onboardingCompleted && !_didRouteCompletedUser) {
      _didRouteCompletedUser = true;
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (mounted) {
          context.go('/home');
        }
      });
    }

    return Scaffold(
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.fromLTRB(20, 24, 20, 28),
          child: Form(
            key: _formKey,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const AppStatusChip(label: '30초 설정'),
                const SizedBox(height: 12),
                Text('내 대화 기준 설정',
                    style: Theme.of(context).textTheme.headlineLarge),
                const SizedBox(height: 12),
                Text(
                  '답장은 상대만 보고 만들면 어색합니다. 먼저 내 말투와 연애 스타일을 정해두면 추천 답장이 내 방식에 더 가까워집니다.',
                  style: Theme.of(context).textTheme.bodyMedium,
                ),
                const SizedBox(height: 18),
                if (bootstrapAsync.isLoading)
                  const SectionCard(
                    child: Center(child: CircularProgressIndicator()),
                  )
                else ...[
                  SectionCard(
                    radius: 26,
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text('기본 정보',
                            style: Theme.of(context).textTheme.titleLarge),
                        const SizedBox(height: 14),
                        TextFormField(
                          controller: _nicknameController,
                          decoration: const InputDecoration(
                            labelText: '내 별칭',
                            helperText: '선택 입력입니다. 실명 대신 별칭을 권장합니다.',
                          ),
                          maxLength: 40,
                        ),
                        DropdownButtonFormField<String>(
                          value: _speechStyle,
                          decoration: const InputDecoration(labelText: '내 말투'),
                          items: const [
                            DropdownMenuItem(
                              value: '짧고 자연스럽게',
                              child: Text('짧고 자연스럽게'),
                            ),
                            DropdownMenuItem(
                              value: '다정하고 부드럽게',
                              child: Text('다정하고 부드럽게'),
                            ),
                            DropdownMenuItem(
                              value: '장난스럽고 가볍게',
                              child: Text('장난스럽고 가볍게'),
                            ),
                            DropdownMenuItem(
                              value: '차분하고 예의 있게',
                              child: Text('차분하고 예의 있게'),
                            ),
                          ],
                          onChanged: (value) {
                            if (value != null) {
                              setState(() => _speechStyle = value);
                            }
                          },
                        ),
                        const SizedBox(height: 8),
                        DropdownButtonFormField<String>(
                          value: _datingStyle,
                          decoration:
                              const InputDecoration(labelText: '연애 스타일'),
                          items: const [
                            DropdownMenuItem(
                              value: '천천히 확인하면서 대화 이어가기',
                              child: Text('천천히 확인하면서 대화 이어가기'),
                            ),
                            DropdownMenuItem(
                              value: '설레는 분위기를 자연스럽게 만들기',
                              child: Text('설레는 분위기를 자연스럽게 만들기'),
                            ),
                            DropdownMenuItem(
                              value: '약속으로 이어지는 흐름 만들기',
                              child: Text('약속으로 이어지는 흐름 만들기'),
                            ),
                            DropdownMenuItem(
                              value: '상대 속도에 맞추기',
                              child: Text('상대 속도에 맞추기'),
                            ),
                          ],
                          onChanged: (value) {
                            if (value != null) {
                              setState(() => _datingStyle = value);
                            }
                          },
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: 12),
                  SectionCard(
                    radius: 26,
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text('조언 방식',
                            style: Theme.of(context).textTheme.titleLarge),
                        const SizedBox(height: 14),
                        SegmentedButton<String>(
                          segments: const [
                            ButtonSegment(
                              value: 'SUPPORTIVE',
                              label: Text('부드럽게'),
                            ),
                            ButtonSegment(
                              value: 'BALANCED',
                              label: Text('균형'),
                            ),
                            ButtonSegment(
                              value: 'REALITY_CHECK',
                              label: Text('현실 체크'),
                            ),
                          ],
                          selected: {_guidanceLevel},
                          onSelectionChanged: (values) {
                            setState(() => _guidanceLevel = values.first);
                          },
                        ),
                        const SizedBox(height: 14),
                        TextFormField(
                          controller: _preferredPartnerStyleController,
                          decoration: const InputDecoration(
                            labelText: '원하는 상대 스타일',
                            helperText: '예: 다정하게 표현하는 사람, 대화를 이어주는 사람',
                          ),
                          maxLength: 160,
                        ),
                        TextFormField(
                          controller: _avoidAdviceController,
                          decoration: const InputDecoration(
                            labelText: '피하고 싶은 조언',
                            helperText: '예: 단정하지 않기, 재촉하지 않기',
                          ),
                          maxLength: 160,
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: 18),
                  FilledButton(
                    onPressed: _isSubmitting ? null : _submit,
                    child: Text(_isSubmitting ? '저장 중' : '저장하고 시작'),
                  ),
                  const SizedBox(height: 8),
                  TextButton(
                    onPressed: _isSubmitting ? null : _startWithDefaults,
                    child: const Text('기본값으로 바로 시작'),
                  ),
                ],
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

    await _saveProfile(
      nickname: _blankToNull(_nicknameController.text),
      speechStyle: _speechStyle,
      datingStyle: _datingStyle,
      guidanceLevel: _guidanceLevel,
      preferredPartnerStyle:
          _blankToNull(_preferredPartnerStyleController.text),
      avoidAdvice: _blankToFallback(
        _avoidAdviceController.text,
        '단정하거나 압박하는 조언은 피하기',
      ),
    );
  }

  Future<void> _startWithDefaults() async {
    await _saveProfile(
      nickname: _blankToNull(_nicknameController.text),
      speechStyle: '짧고 자연스럽게',
      datingStyle: '천천히 확인하면서 대화 이어가기',
      guidanceLevel: 'BALANCED',
      preferredPartnerStyle:
          _blankToNull(_preferredPartnerStyleController.text),
      avoidAdvice: '단정하거나 압박하는 조언은 피하기',
    );
  }

  Future<void> _saveProfile({
    required String? nickname,
    required String speechStyle,
    required String datingStyle,
    required String guidanceLevel,
    required String? preferredPartnerStyle,
    required String avoidAdvice,
  }) async {
    setState(() => _isSubmitting = true);
    try {
      await ref.read(bootstrapApiProvider).updateProfile(
            UpdateProfilePayload(
              nickname: nickname,
              speechStyle: speechStyle,
              datingStyle: datingStyle,
              guidanceLevel: guidanceLevel,
              preferredPartnerStyle: preferredPartnerStyle,
              avoidAdvice: avoidAdvice,
            ),
          );
      ref.invalidate(bootstrapProvider);
      if (mounted) {
        context.go('/home');
      }
    } catch (_) {
      if (mounted) {
        setState(() => _isSubmitting = false);
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('내 정보를 저장하지 못했습니다.')),
        );
      }
    }
  }
}

void _setControllerTextIfEmpty(
    TextEditingController controller, String? value) {
  if (controller.text.isNotEmpty || value == null || value.trim().isEmpty) {
    return;
  }
  controller.text = value;
}

String? _blankToNull(String value) {
  final trimmed = value.trim();
  return trimmed.isEmpty ? null : trimmed;
}

String _blankToFallback(String value, String fallback) {
  final trimmed = value.trim();
  return trimmed.isEmpty ? fallback : trimmed;
}
