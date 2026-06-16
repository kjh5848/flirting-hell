/// 성격 프레임워크의 **단일 소스**.
///
/// 제품 핵심 결정(자체 5축 vs MBTI vs 혼합)은 아직 확정 전이다. 프레임워크를
/// 바꾸려면 이 파일의 [personalityAxes]와 적합도 계산만 손대면 되도록
/// 모든 축 정의를 여기 한 곳에 모은다. 화면 코드에 축을 하드코딩하지 말 것.
///
/// 자세한 근거와 대안은 `docs/product/personality-coaching-spec.md` §2 참고.
library;

class PersonalityAxis {
  const PersonalityAxis({
    required this.id,
    required this.label,
    required this.lowLabel,
    required this.highLabel,
    required this.description,
  });

  /// 점수 맵의 키. 저장/계약에 쓰이므로 변경 시 마이그레이션 필요.
  final String id;

  /// 화면에 보이는 축 이름.
  final String label;

  /// 1점 쪽 극(pole) 라벨.
  final String lowLabel;

  /// 5점 쪽 극 라벨.
  final String highLabel;

  /// 축이 무엇을 재는지 한 줄 설명.
  final String description;
}

/// 점수 범위(양 끝 포함). 1~5점.
const int personalityScoreMin = 1;
const int personalityScoreMax = 5;

/// 자체 5축 "연애 성향" (현재 채택안 — 교체 가능).
const List<PersonalityAxis> personalityAxes = [
  PersonalityAxis(
    id: 'expression',
    label: '표현',
    lowLabel: '신중함',
    highLabel: '적극적',
    description: '호감 표현을 얼마나 먼저·세게 하나',
  ),
  PersonalityAxis(
    id: 'pace',
    label: '속도',
    lowLabel: '천천히',
    highLabel: '빠르게',
    description: '관계 진전 속도 선호',
  ),
  PersonalityAxis(
    id: 'contact',
    label: '연락',
    lowLabel: '가끔',
    highLabel: '자주',
    description: '연락 빈도 선호',
  ),
  PersonalityAxis(
    id: 'emotion',
    label: '감정 표현',
    lowLabel: '은근함',
    highLabel: '직설적',
    description: '감정을 드러내는 방식',
  ),
  PersonalityAxis(
    id: 'values',
    label: '가치관',
    lowLabel: '현실적',
    highLabel: '낭만적',
    description: '연애를 대하는 가치관',
  ),
];

/// 점수 1~5에 대응하는 극 쪽 짧은 라벨(중앙은 "중간").
String poleLabelFor(PersonalityAxis axis, int score) {
  if (score <= 2) return axis.lowLabel;
  if (score >= 4) return axis.highLabel;
  return '중간';
}
