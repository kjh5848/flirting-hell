package com.flirtinghell.analysis.adapter.out.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.flirtinghell.analysis.application.port.out.AnalysisPort;
import com.flirtinghell.analysis.domain.model.InputSourceType;
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
