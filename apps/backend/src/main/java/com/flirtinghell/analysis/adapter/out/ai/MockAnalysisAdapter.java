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
		PartnerAxes axes = derivePartnerAxes(request.rawInput(), strategyId);
		boolean continuing = !request.recentSummaries().isEmpty();

		String summary = continuing
				? "이전 대화에 이어, " + summary(request.rawInput())
				: summary(request.rawInput());

		// 메모리 Phase A: 상대 유형에 맞춰 주의·다음행동·이유를 개인화한다(DoD #4).
		List<String> warnings = new ArrayList<>(List.of(
				"지금 바로 마음을 확인하려고 몰아붙이지 마세요.",
				"답장이 늦어도 추궁처럼 보이는 표현은 피하세요."
		));
		if (axes.pace() >= 4) {
			warnings.add("상대가 빠르게 진도를 낼 수 있으니, 내 속도도 존중하며 맞춰가세요.");
		}

		String nextAction = continuing
				? "이전 흐름을 이어, " + personalizedNextAction(axes)
				: personalizedNextAction(axes);

		return new AnalysisDraft(
				sourceType,
				participantSummary(request.rawInput()),
				summary,
				"상대가 편하게 반응하는 중이라 부담 없이 한 번 더 이어갈 수 있어요.",
				strategyId,
				List.copyOf(warnings),
				primaryReply(strategyId, request.rawInput()),
				alternativeReplies(strategyId),
				personalizedReplyReason(axes),
				nextAction,
				partnerTypeJson(axes, request.rawInput())
		);
	}

	/// 상대 5축 성향을 입력에서 결정적으로 추정한다(mock). 실 LLM 연동 시 모델이 채운다.
	private PartnerAxes derivePartnerAxes(String rawInput, StrategyId strategyId) {
		int expression = rawInput.contains("ㅋㅋ") || rawInput.contains("!") ? 4 : 3;
		int pace = rawInput.contains("빨리") || rawInput.contains("자주") ? 4 : 2;
		int contact = rawInput.contains("나:") && rawInput.contains("상대:") ? 4 : 3;
		int emotion = rawInput.contains("좋아") || rawInput.contains("설레") ? 4 : 3;
		int values = strategyId == StrategyId.MARRIAGE_VALUES ? 4 : 3;
		return new PartnerAxes(expression, pace, contact, emotion, values);
	}

	private String partnerTypeJson(PartnerAxes axes, String rawInput) {
		String summary = rawInput.contains("집") || rawInput.contains("쉬")
				? "편안하게 일상을 공유하는 반응이라 천천히 다가가기 좋은 유형으로 보여요."
				: "반응이 가벼우면서 열려 있어 부담 없이 대화를 이어가기 좋은 유형으로 보여요.";
		return String.format(
				Locale.ROOT,
				"{\"expression\":%d,\"pace\":%d,\"contact\":%d,\"emotion\":%d,\"values\":%d,\"summary\":\"%s\"}",
				axes.expression(), axes.pace(), axes.contact(), axes.emotion(), axes.values(), summary
		);
	}

	private String personalizedNextAction(PartnerAxes axes) {
		if (axes.pace() >= 4) {
			return "다음 약속은 서두르지 말고 가벼운 만남부터 제안합니다.";
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

	private record PartnerAxes(int expression, int pace, int contact, int emotion, int values) {
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

	private String summary(String rawInput) {
		if (rawInput.contains("집") || rawInput.contains("쉬")) {
			return "상대가 집에서 쉬는 흐름이라 가볍게 대화를 이어갈 수 있어요.";
		}
		return "상대 반응을 더 확인하면서 가볍게 대화를 이어갈 수 있어요.";
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
