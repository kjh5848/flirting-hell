import 'dart:convert';

import 'personality_axes.dart';

/// 전역 프로필에 추가될 성향 데이터. (이번 단계는 로컬 상태만, 백엔드 영속화 보류)
class PersonalityProfile {
  const PersonalityProfile({
    required this.self,
    required this.ideal,
  });

  /// 내 성향. axisId -> 1..5.
  final Map<String, int> self;

  /// 내 이상형(원하는 상대 성향). axisId -> 1..5.
  final Map<String, int> ideal;

  /// 모든 축을 중앙값(3)으로 채운 기본 프로필.
  factory PersonalityProfile.neutral() {
    final base = {for (final axis in personalityAxes) axis.id: 3};
    return PersonalityProfile(self: {...base}, ideal: {...base});
  }

  /// 서버에 저장된 JSON 문자열(없거나 손상 시 중앙값)에서 복원한다.
  factory PersonalityProfile.fromStored({String? self, String? ideal}) {
    return PersonalityProfile(
      self: _decodeAxes(self),
      ideal: _decodeAxes(ideal),
    );
  }

  String get selfJson => jsonEncode(self);
  String get idealJson => jsonEncode(ideal);

  PersonalityProfile copyWith({
    Map<String, int>? self,
    Map<String, int>? ideal,
  }) {
    return PersonalityProfile(
      self: self ?? this.self,
      ideal: ideal ?? this.ideal,
    );
  }
}

/// 한 축에서 이상형과 상대의 차이.
class AxisGap {
  const AxisGap({
    required this.axis,
    required this.idealValue,
    required this.partnerValue,
  });

  final PersonalityAxis axis;
  final int idealValue;
  final int partnerValue;

  int get gap => (idealValue - partnerValue).abs();
}

/// 이상형 ↔ 상대 유형의 적합도(결정적 계산 결과).
class Compatibility {
  const Compatibility({
    required this.score,
    required this.perAxis,
  });

  /// 0~100. 높을수록 이상형에 가깝다.
  final int score;
  final List<AxisGap> perAxis;

  /// 가장 잘 맞는 축(갭 최소). 동률이면 먼저 정의된 축.
  AxisGap get bestMatch =>
      perAxis.reduce((a, b) => b.gap < a.gap ? b : a);

  /// 가장 주의할 축(갭 최대).
  AxisGap get biggestGap =>
      perAxis.reduce((a, b) => b.gap > a.gap ? b : a);
}

/// 이상형과 상대 유형의 적합도를 **결정적으로** 계산한다.
///
/// 각 축의 최대 차이는 [personalityScoreMax] - [personalityScoreMin] = 4.
/// 전체 차이 합을 최대 가능 차이로 정규화해 0~100 점수로 환산한다.
/// (LLM이 아니라 순수 함수 → 단위 테스트로 검증 가능.)
Compatibility computeCompatibility({
  required Map<String, int> ideal,
  required Map<String, int> partner,
  List<PersonalityAxis> axes = personalityAxes,
}) {
  final gaps = <AxisGap>[];
  var totalGap = 0;

  for (final axis in axes) {
    final idealValue = _clampScore(ideal[axis.id] ?? _midScore);
    final partnerValue = _clampScore(partner[axis.id] ?? _midScore);
    final axisGap = AxisGap(
      axis: axis,
      idealValue: idealValue,
      partnerValue: partnerValue,
    );
    gaps.add(axisGap);
    totalGap += axisGap.gap;
  }

  const maxGapPerAxis = personalityScoreMax - personalityScoreMin;
  final maxTotalGap = maxGapPerAxis * axes.length;
  final score = maxTotalGap == 0
      ? 100
      : (100 * (1 - totalGap / maxTotalGap)).round();

  return Compatibility(score: score, perAxis: gaps);
}

/// JSON 문자열을 축 점수 맵으로 디코드한다. 누락 축은 중앙값(3), 손상 시 전부 중앙값.
Map<String, int> _decodeAxes(String? raw) {
  final result = {for (final axis in personalityAxes) axis.id: _midScore};
  if (raw == null || raw.trim().isEmpty) {
    return result;
  }
  try {
    final decoded = jsonDecode(raw);
    if (decoded is Map) {
      for (final entry in decoded.entries) {
        final key = entry.key.toString();
        final value = entry.value;
        if (result.containsKey(key) && value is num) {
          result[key] = _clampScore(value.round());
        }
      }
    }
  } catch (_) {
    // 손상된 값은 무시하고 기본값 유지.
  }
  return result;
}

const int _midScore = 3;

int _clampScore(int value) {
  if (value < personalityScoreMin) return personalityScoreMin;
  if (value > personalityScoreMax) return personalityScoreMax;
  return value;
}
