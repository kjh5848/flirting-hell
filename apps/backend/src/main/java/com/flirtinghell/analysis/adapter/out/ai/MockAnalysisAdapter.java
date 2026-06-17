package com.flirtinghell.analysis.adapter.out.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.flirtinghell.analysis.application.port.out.AnalysisPort;
import com.flirtinghell.analysis.domain.model.InputSourceType;
import com.flirtinghell.consultation.domain.model.RelationshipStage;
import com.flirtinghell.consultation.domain.model.StrategyId;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "flirting-hell.ai.provider", havingValue = "mock", matchIfMissing = true)
class MockAnalysisAdapter implements AnalysisPort {

	@Override
	public AnalysisDraft analyze(AnalysisRequest request) {
		InputSourceType sourceType = detectSourceType(request.rawInput());
		StrategyId strategyId = request.requestedStrategyId() == null
				? StrategyId.DEVELOP_ROMANCE
				: request.requestedStrategyId();
		Scenario scenario = detectScenario(request.rawInput(), strategyId);
		PartnerAxes axes = scenario.axes();
		boolean continuing = !request.recentSummaries().isEmpty();

		String summary = continuing
				? "이전 대화에 이어, " + scenario.summaryText()
				: scenario.summaryText();

		// 메모리 Phase A: 상대 유형에 맞춰 주의·다음행동·이유를 개인화한다(DoD #4).
		List<String> warnings = new ArrayList<>(List.of(
				"지금 바로 마음을 확인하려고 몰아붙이지 마세요.",
				"답장이 늦어도 추궁처럼 보이는 표현은 피하세요."
		));
		if (axes.pace() >= 4) {
			warnings.add("상대가 빠르게 진도를 낼 수 있으니, 내 속도도 존중하며 맞춰가세요.");
		}
		if (scenario.extraWarning() != null) {
			warnings.add(scenario.extraWarning());
		}

		String nextAction = continuing
				? "이전 흐름을 이어, " + personalizedNextAction(axes)
				: personalizedNextAction(axes);

		return new AnalysisDraft(
				sourceType,
				participantSummary(request.rawInput()),
				summary,
				scenario.currentState(),
				strategyId,
				List.copyOf(warnings),
				primaryReply(strategyId, request.rawInput()),
				alternativeReplies(strategyId),
				personalizedReplyReason(axes),
				nextAction,
				partnerTypeJson(axes, scenario.typeSummary())
		);
	}

	@Override
	public String refineReply(RefineRequest request) {
		// mock: 방향별 톤 예시를 결정적으로 반환(실 LLM은 previousReply를 실제로 다시 씀).
		return switch (request.direction()) {
			case LIGHTER -> "오 그래 ㅋㅋ 별거 아니야, 편하게 생각해~";
			case MORE_SERIOUS -> "사실 너랑 이렇게 얘기하는 거 좋아서 더 알아가고 싶어.";
			case SLOWER -> "급할 거 없어 ㅋㅋ 천천히 알아가자.";
			case BOLDER -> "솔직히 말하면 너 보고 싶었어. 이번 주에 시간 어때?";
		};
	}

	@Override
	public PlanDraft suggestPlan(PlanRequest request) {
		// 코스는 관계 단계별로, 확인 포인트는 식습관·취향 기반(궁합 가늠). 결정적(mock).
		List<String> foodCheckPoints = List.of(
				"식사 전에 '혹시 못 먹는 거 있어?' 가볍게 물어보기 — 알레르기·비건·매운맛 취향이 한 번에 드러나요.",
				"좋아하는 음식 한두 개 알아두기 — 다음 약속을 자연스럽게 잡는 핑계가 돼요.",
				"메뉴 정하는 방식(빨리 정함/신중함)도 성향 힌트예요. 나와 페이스가 맞는지 같이 봐요."
		);

		RelationshipStage stage = request.relationshipStage();
		if (stage == RelationshipStage.FIRST_CONTACT
				|| stage == RelationshipStage.TALKING
				|| stage == RelationshipStage.UNKNOWN) {
			return new PlanDraft(
					"부담 없는 첫 만남",
					List.of(
							new PlanStep("가벼운 카페", "1~2시간 정도, 자리 옮기기 쉬운 곳으로 부담을 낮춥니다."),
							new PlanStep("짧은 산책", "대화가 잘 풀리면 근처를 천천히 걸으며 자연스럽게 이어갑니다."),
							new PlanStep("간단한 식사(선택)", "분위기가 좋으면 가벼운 한 끼로 자연스럽게 연장합니다.")
					),
					foodCheckPoints,
					List.of(
							"첫 만남부터 코스를 길게 잡지 마세요. 여운을 남기는 편이 좋아요.",
							"맛집 예약을 강하게 밀어붙이기보다 상대 취향을 먼저 확인하세요."
					)
			);
		}
		if (stage == RelationshipStage.RECOVERY) {
			return new PlanDraft(
					"부드러운 재회",
					List.of(
							new PlanStep("편한 카페", "지난 어색함을 덜 수 있는 익숙하고 편안한 장소를 고릅니다."),
							new PlanStep("가벼운 디저트", "무겁지 않게, 짧고 기분 좋게 마무리합니다.")
					),
					foodCheckPoints,
					List.of(
							"지난 일을 길게 곱씹지 마세요. 가볍게 다시 시작하는 자리로 둡니다.",
							"무리한 분위기 연출보다 편안함을 우선하세요."
					)
			);
		}
		return new PlanDraft(
				"한 걸음 더 가까워지는 데이트",
				List.of(
						new PlanStep("취향 맞춘 식사", "상대가 좋아하는 음식 기준으로 식당을 고르면 배려가 전달됩니다."),
						new PlanStep("함께 하는 활동", "전시·산책·소품샵처럼 대화가 이어지는 활동을 곁들입니다."),
						new PlanStep("가벼운 마무리", "카페나 야경처럼 여운 있는 마무리로 다음을 기약합니다.")
				),
				foodCheckPoints,
				List.of(
						"좋은 흐름이어도 상대 속도를 앞지르지 마세요.",
						"비용·일정은 미리 가볍게 공유해 부담을 줄이세요."
				)
		);
	}

	@Override
	public String coachReply(CoachRequest request) {
		String message = request.userMessage();
		boolean first = request.history().isEmpty();

		// 공감 → 작은 질문/제안 순서로, 입력 키워드에 따라 결정적으로(mock).
		if (first) {
			return "편하게 얘기해도 돼요. 지금 그 사람과의 상황에서 제일 신경 쓰이는 건 뭐예요?";
		}
		if (containsAny(message, "불안", "걱정", "초조", "떨려")) {
			return "그 마음 충분히 그럴 수 있어요. 상대 반응을 단정하기엔 일러요. "
					+ "지금 확인하고 싶은 한 가지만 골라볼까요?";
		}
		if (containsAny(message, "답장", "늦", "읽씹", "연락")) {
			return "답장 텀은 그날 컨디션일 때가 많아요. 재촉 대신, 다음에 가볍게 이어갈 한마디를 같이 만들어볼까요?";
		}
		if (containsAny(message, "약속", "만나", "데이트", "볼까")) {
			return "좋아요. 부담 없는 선택지부터 제안하면 성공률이 높아요. 상대가 좋아할 만한 활동 하나 떠오르는 거 있어요?";
		}
		if (containsAny(message, "싫", "거절", "차였", "관심 없")) {
			return "속상했겠어요. 거절처럼 느껴져도 상대 사정일 수 있어요. 무리해서 매달리기보다 한 발 여유를 두는 걸 권해요.";
		}
		return "그렇군요. 조금 더 들려줄래요? 어떤 결과를 가장 바라는지 알면 다음 한마디를 같이 골라볼 수 있어요.";
	}

	/// 입력 신호로 상황 시나리오를 결정적으로 감지한다(mock). 시나리오마다 상대 5축
	/// 유형·요약·주의가 현실적으로 달라져 키 없이도 다양한 케이스를 데모/검증할 수 있다.
	private Scenario detectScenario(String rawInput, StrategyId strategyId) {
		int values = strategyId == StrategyId.MARRIAGE_VALUES ? 4 : 3;
		if (containsAny(rawInput, "미안", "사과", "어색", "오랜만", "뜸")) {
			return new Scenario(
					new PartnerAxes(2, 2, 2, 3, values),
					"잠시 거리가 생긴 흐름이라 부드럽게 회복하는 게 좋아요.",
					"호감 단정은 어렵지만, 천천히 다시 편해지는 단계예요.",
					"표현을 아끼고 천천히 가는 유형으로 보여요.",
					"회복 단계에서는 지난 일을 길게 곱씹는 표현을 피하세요."
			);
		}
		if (containsAny(rawInput, "바빠", "나중에", "모르겠", "글쎄", "괜찮아")) {
			return new Scenario(
					new PartnerAxes(2, 2, 2, 2, values),
					"상대가 거리를 두는 신호라 재촉은 피하는 게 좋아요.",
					"지금은 한 발 물러나 여유를 주는 게 안전한 상태예요.",
					"신중하고 거리를 두는 유형으로 보여요.",
					"거리 신호가 보일 때 연락 빈도를 늘리지 마세요."
			);
		}
		if (containsAny(rawInput, "약속", "언제", "만나", "볼까", "시간")) {
			return new Scenario(
					new PartnerAxes(3, 4, 3, 3, values),
					"약속으로 이어질 타이밍을 보고 있어요.",
					"약속 제안을 받아들일 가능성이 보이는 상태예요.",
					"관계 속도가 빠른 편이라 약속을 반기는 유형으로 보여요.",
					null
			);
		}
		if (containsAny(rawInput, "좋아", "재밌", "보고싶", "설레", "ㅋㅋ")) {
			return new Scenario(
					new PartnerAxes(4, 3, 4, 4, values),
					"호감 신호가 보이는 편이라 자연스럽게 이어가기 좋아요.",
					"상대가 편하게 호응하는 우호적인 상태예요.",
					"표현이 적극적이고 감정을 잘 드러내는 유형으로 보여요.",
					null
			);
		}
		return new Scenario(
				new PartnerAxes(3, 2, 3, 3, values),
				"편하게 일상을 나누는 흐름이라 가볍게 이어갈 수 있어요.",
				"상대가 편하게 반응하는 중이라 부담 없이 한 번 더 이어갈 수 있어요.",
				"편안하게 일상을 공유하는 유형으로 보여요.",
				null
		);
	}

	private String partnerTypeJson(PartnerAxes axes, String typeSummary) {
		return String.format(
				Locale.ROOT,
				"{\"expression\":%d,\"pace\":%d,\"contact\":%d,\"emotion\":%d,\"values\":%d,\"summary\":\"%s\"}",
				axes.expression(), axes.pace(), axes.contact(), axes.emotion(), axes.values(), typeSummary
		);
	}

	private String personalizedNextAction(PartnerAxes axes) {
		if (axes.pace() >= 4) {
			return "다음 약속은 서두르지 말고 가벼운 만남부터 제안합니다.";
		}
		if (axes.contact() <= 2) {
			return "연락 빈도를 늘리기보다, 상대가 답할 여유를 두고 기다립니다.";
		}
		return "상대가 답하면 음식, 산책, 영화처럼 가벼운 선택지로 약속 가능성을 봅니다.";
	}

	private String personalizedReplyReason(PartnerAxes axes) {
		if (axes.pace() >= 4) {
			return "상대 속도가 빠른 편이라, 호응은 하되 한 박자 늦춰 부담을 줄이는 편이 안전합니다.";
		}
		if (axes.expression() <= 2) {
			return "상대가 표현을 아끼는 편이라, 먼저 가볍게 마음을 보여주면 대화가 편해집니다.";
		}
		return "상대가 편한 상태를 말했으니, 확인 질문보다 일상 질문으로 자연스럽게 이어가는 편이 안전합니다.";
	}

	private static boolean containsAny(String rawInput, String... keywords) {
		for (String keyword : keywords) {
			if (rawInput.contains(keyword)) {
				return true;
			}
		}
		return false;
	}

	private record PartnerAxes(int expression, int pace, int contact, int emotion, int values) {
	}

	private record Scenario(
			PartnerAxes axes,
			String summaryText,
			String currentState,
			String typeSummary,
			String extraWarning
	) {
	}

	private InputSourceType detectSourceType(String rawInput) {
		String normalized = rawInput.toLowerCase(Locale.ROOT);
		if (rawInput.contains("나:") || rawInput.contains("상대:") || rawInput.contains("오전") || rawInput.contains("오후")) {
			return InputSourceType.KAKAO;
		}
		if (normalized.contains("dm") || normalized.contains("인스타")) {
			return InputSourceType.DM;
		}
		if (normalized.contains("telegram") || normalized.contains("텔레그램")) {
			return InputSourceType.TELEGRAM;
		}
		if (normalized.contains("문자") || normalized.contains("sms")) {
			return InputSourceType.SMS;
		}
		if (normalized.contains("상황") || !rawInput.contains(":")) {
			return InputSourceType.SITUATION;
		}
		return InputSourceType.UNKNOWN;
	}

	private String participantSummary(String rawInput) {
		if (rawInput.contains("나:") && rawInput.contains("상대:")) {
			return "나와 상대 발화가 구분되어 있어요.";
		}
		return "발화자 구분은 일부 애매해서 요약 중심으로 봅니다.";
	}

	private String primaryReply(StrategyId strategyId, String rawInput) {
		if (strategyId == StrategyId.MAKE_PLAN && (rawInput.contains("집") || rawInput.contains("쉬"))) {
			return "오 좋다 ㅋㅋ 그럼 오늘은 집에서 충전하는 날이네. 저녁은 뭐 먹을 생각이야?";
		}
		if (strategyId == StrategyId.CHECK_RELATIONSHIP_STATUS) {
			return "그랬구나 ㅋㅋ 요즘은 누구랑 제일 자주 놀아?";
		}
		if (strategyId == StrategyId.SLOW_DOWN) {
			return "오 쉬는 날이면 제대로 쉬어야지 ㅋㅋ 오늘은 그냥 편하게 보내";
		}
		return "오 좋다 ㅋㅋ 오늘은 편하게 쉬는 날이네. 뭐하면서 쉬고 있어?";
	}

	private List<String> alternativeReplies(StrategyId strategyId) {
		if (strategyId == StrategyId.MAKE_PLAN) {
			return List.of(
					"집에서 쉬는 날 좋지 ㅋㅋ 나는 이런 날 맛있는 거 먹으면 바로 회복돼",
					"그럼 오늘은 무리하지 말고 쉬어 ㅋㅋ 나중에 컨디션 좋을 때 맛있는 거 먹자"
			);
		}
		return List.of(
				"오 그렇구나 ㅋㅋ 오늘은 편하게 보내는 중이네",
				"쉬는 날이면 좋지 ㅋㅋ 뭐하면서 쉬는 게 제일 좋아?"
		);
	}
}
