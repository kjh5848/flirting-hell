export const relationshipStages = ["처음 연락", "썸", "데이트 전", "데이트 후", "관계 회복"] as const;
export const conversationGoals = ["대화 이어가기", "호감 표현", "약속 잡기", "실수 만회", "상대 마음 확인"] as const;
export const replyIntensities = ["순한맛", "설렘맛", "직진맛"] as const;
export const guidanceModes = ["응원 위주", "균형 조언", "현실 체크"] as const;
export const toneModes = ["자동 분석", "직접 설정", "이번엔 반영 안 함"] as const;

export const datingStyleOptions = ["다정한", "표현 많은", "편안한", "재밌는", "깊은 대화", "자유로운"] as const;
export const preferredPartnerStyleOptions = ["상냥한", "애교 있는", "차분한", "시크한", "털털한", "대화가 잘 통하는"] as const;
export const difficultPartnerStyleOptions = ["무뚝뚝한", "연락이 느린", "표현이 적은", "너무 가벼운", "감정 기복이 큰"] as const;
export const attractionReasonOptions = ["외모", "대화", "분위기", "배려", "설렘", "아직 모르겠음"] as const;

export type RelationshipStage = (typeof relationshipStages)[number];
export type ConversationGoal = (typeof conversationGoals)[number];
export type ReplyIntensity = (typeof replyIntensities)[number];
export type GuidanceMode = (typeof guidanceModes)[number];
export type ToneMode = (typeof toneModes)[number];
