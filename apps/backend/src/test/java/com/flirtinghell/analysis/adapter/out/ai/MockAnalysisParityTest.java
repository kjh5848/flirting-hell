package com.flirtinghell.analysis.adapter.out.ai;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flirtinghell.analysis.application.port.out.AnalysisPort;
import com.flirtinghell.analysis.application.port.out.AnalysisPort.AnalysisDraft;
import com.flirtinghell.analysis.application.port.out.AnalysisPort.AnalysisRequest;
import com.flirtinghell.consultation.domain.model.RelationshipStage;
import com.flirtinghell.consultation.domain.model.StrategyId;
import org.junit.jupiter.api.Test;

/// mock-first 보장: mock이 계약(AnalysisDraft)의 모든 필드를 유효하게 채우는지 고정한다.
/// 계약이 늘면 이 테스트가 깨져 mock도 같이 채우도록 강제한다 → 실 LLM 전환 시 "빈칸" 없음.
class MockAnalysisParityTest {

	private final AnalysisPort mock = new MockAnalysisAdapter();
	private final ObjectMapper objectMapper = new ObjectMapper();

	private static final List<String> SCENARIO_INPUTS = List.of(
			"나: 오늘 뭐해?\n상대: 그냥 집에 있어 ㅋㅋ", // 호감
			"상대: 우리 언제 만날까?", // 약속
			"상대: 요즘 좀 바빠서 나중에 보자", // 거리두기
			"저번에 어색해진 거 미안하다고 하고 싶어", // 회복
			"상대랑 오늘 처음 연락했어" // 일상(기본)
	);

	@Test
	void mockFillsEveryContractFieldForAllScenarios() {
		for (String input : SCENARIO_INPUTS) {
			AnalysisDraft draft = mock.analyze(request(input, List.of()));

			assertThat(draft.sourceType()).as("sourceType: %s", input).isNotNull();
			assertThat(draft.recommendedStrategyId()).as("strategy: %s", input).isNotNull();
			assertThat(draft.participantSummary()).as("participantSummary: %s", input).isNotBlank();
			assertThat(draft.summary()).as("summary: %s", input).isNotBlank();
			assertThat(draft.currentState()).as("currentState: %s", input).isNotBlank();
			assertThat(draft.primaryReply()).as("primaryReply: %s", input).isNotBlank();
			assertThat(draft.replyReason()).as("replyReason: %s", input).isNotBlank();
			assertThat(draft.nextAction()).as("nextAction: %s", input).isNotBlank();
			assertThat(draft.warnings()).as("warnings: %s", input).isNotEmpty();
			assertThat(draft.alternativeReplies()).as("alternatives: %s", input).isNotEmpty();

			assertValidPartnerType(draft.partnerType(), input);
		}
	}

	@Test
	void recentSummariesTriggerContinuityPrefix() {
		AnalysisDraft draft = mock.analyze(request(
				"상대: 그래서 언제 볼까?",
				List.of("지난번엔 가볍게 인사만 나눴어요.")
		));
		assertThat(draft.summary()).startsWith("이전 대화에 이어");
		assertThat(draft.nextAction()).startsWith("이전 흐름을 이어");
	}

	@Test
	void differentScenariosProduceDifferentPartnerTypes() {
		String warm = mock.analyze(request("좋아 ㅋㅋ 재밌다", List.of())).partnerType();
		String distant = mock.analyze(request("요즘 바빠서 나중에", List.of())).partnerType();
		assertThat(warm).isNotEqualTo(distant);
	}

	@Test
	void refineReturnsDistinctNonBlankReplyPerDirection() {
		java.util.Set<String> replies = new java.util.HashSet<>();
		for (AnalysisPort.RefineDirection direction : AnalysisPort.RefineDirection.values()) {
			String reply = mock.refineReply(
					new AnalysisPort.RefineRequest("오 좋다 ㅋㅋ 오늘 뭐해?", direction, null));
			assertThat(reply).as("refine %s", direction).isNotBlank();
			replies.add(reply);
		}
		assertThat(replies).hasSize(AnalysisPort.RefineDirection.values().length);
	}

	@Test
	void coachOpensWithListeningThenRespondsToKeywords() {
		String opener = mock.coachReply(new AnalysisPort.CoachRequest(
				List.of(), "그 사람 때문에 고민이야", "호감 확인", null, null));
		assertThat(opener).isNotBlank();

		AnalysisPort.CoachRequest anxious = new AnalysisPort.CoachRequest(
				List.of(new AnalysisPort.CoachMessage(AnalysisPort.CoachRole.USER, "안녕")),
				"답장이 늦어서 너무 불안해",
				"호감 확인",
				null,
				null
		);
		String reply = mock.coachReply(anxious);
		assertThat(reply).isNotBlank();
		// 후속 턴은 오프너와 달라야 한다(공감/맥락 반영).
		assertThat(reply).isNotEqualTo(opener);
	}

	@Test
	void planIncludesCourseAndFoodCompatibilityCheckPoints() {
		for (com.flirtinghell.consultation.domain.model.RelationshipStage stage
				: com.flirtinghell.consultation.domain.model.RelationshipStage.values()) {
			AnalysisPort.PlanDraft plan = mock.suggestPlan(new AnalysisPort.PlanRequest(
					stage, "호감 확인", null, null));
			assertThat(plan.theme()).as("theme %s", stage).isNotBlank();
			assertThat(plan.steps()).as("steps %s", stage).isNotEmpty();
			assertThat(plan.steps()).allSatisfy(step -> {
				assertThat(step.title()).isNotBlank();
				assertThat(step.detail()).isNotBlank();
			});
			assertThat(plan.cautions()).as("cautions %s", stage).isNotEmpty();
			// 핵심: 식습관/음식 기반 궁합 확인 포인트가 들어있어야 한다.
			assertThat(plan.checkPoints()).as("checkPoints %s", stage).isNotEmpty();
			assertThat(plan.checkPoints().stream()
					.anyMatch(point -> point.contains("음식") || point.contains("못 먹는") || point.contains("메뉴")))
					.as("food check point present %s", stage)
					.isTrue();
		}
	}

	private void assertValidPartnerType(String partnerType, String input) {
		assertThat(partnerType).as("partnerType present: %s", input).isNotBlank();
		try {
			JsonNode node = objectMapper.readTree(partnerType);
			for (String axis : List.of("expression", "pace", "contact", "emotion", "values")) {
				assertThat(node.has(axis)).as("axis %s present: %s", axis, input).isTrue();
				int value = node.get(axis).asInt();
				assertThat(value).as("axis %s range: %s", axis, input).isBetween(1, 5);
			}
			assertThat(node.get("summary").asText()).as("type summary: %s", input).isNotBlank();
		} catch (Exception exception) {
			throw new AssertionError("partnerType is not valid JSON for input: " + input, exception);
		}
	}

	private AnalysisRequest request(String rawInput, List<String> recentSummaries) {
		return new AnalysisRequest(
				"지우",
				RelationshipStage.TALKING,
				"호감인지 확인",
				"부담 금지",
				StrategyId.MAKE_PLAN,
				rawInput,
				null,
				null,
				recentSummaries,
				null
		);
	}
}
