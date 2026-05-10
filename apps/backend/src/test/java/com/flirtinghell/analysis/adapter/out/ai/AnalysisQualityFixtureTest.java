package com.flirtinghell.analysis.adapter.out.ai;

import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flirtinghell.analysis.application.port.out.AnalysisPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

class AnalysisQualityFixtureTest {

	private static final ObjectMapper objectMapper = new ObjectMapper();
	private static final List<AnalysisQualityFixture> fixtures = AnalysisQualityFixtures.load(objectMapper);

	@Test
	void fixturesCoverCoreMvpScenarios() {
		assertThat(fixtures).hasSizeGreaterThanOrEqualTo(6);
		assertThat(fixtures).extracting(AnalysisQualityFixture::id).doesNotHaveDuplicates();
		assertThat(fixtures).extracting(AnalysisQualityFixture::expectedSourceType)
				.contains(
						com.flirtinghell.analysis.domain.model.InputSourceType.KAKAO,
						com.flirtinghell.analysis.domain.model.InputSourceType.DM,
						com.flirtinghell.analysis.domain.model.InputSourceType.TELEGRAM,
						com.flirtinghell.analysis.domain.model.InputSourceType.SMS,
						com.flirtinghell.analysis.domain.model.InputSourceType.SITUATION
				);
		assertThat(fixtures).extracting(AnalysisQualityFixture::requestedStrategyId)
				.contains(
						com.flirtinghell.consultation.domain.model.StrategyId.DEVELOP_ROMANCE,
						com.flirtinghell.consultation.domain.model.StrategyId.CHECK_RELATIONSHIP_STATUS,
						com.flirtinghell.consultation.domain.model.StrategyId.MAKE_PLAN,
						com.flirtinghell.consultation.domain.model.StrategyId.MARRIAGE_VALUES,
						com.flirtinghell.consultation.domain.model.StrategyId.SLOW_DOWN
				);
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("fixtures")
	void mockProviderProducesSafeContractForEveryFixture(AnalysisQualityFixture fixture) {
		MockAnalysisAdapter adapter = new MockAnalysisAdapter();

		AnalysisPort.AnalysisDraft draft = adapter.analyze(fixture.toRequest());

		assertValidDraft(fixture, draft);
		assertThat(draft.sourceType()).isEqualTo(fixture.expectedSourceType());
		assertThat(draft.recommendedStrategyId()).isEqualTo(fixture.requestedStrategyId());
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("fixtures")
	void fakeLlmProviderMapsFixtureResponseThroughSharedAdapter(AnalysisQualityFixture fixture) {
		LlmClient fakeClient = prompt -> fixture.expectedResponseJson(objectMapper);
		LlmAnalysisAdapter adapter = new LlmAnalysisAdapter(objectMapper, fakeClient);

		AnalysisPort.AnalysisDraft draft = adapter.analyze(fixture.toRequest());

		assertValidDraft(fixture, draft);
		assertThat(draft.sourceType()).isEqualTo(fixture.expectedSourceType());
		assertThat(draft.recommendedStrategyId()).isEqualTo(fixture.expectedRecommendedStrategyId());
	}

	static Stream<AnalysisQualityFixture> fixtures() {
		return fixtures.stream();
	}

	private void assertValidDraft(AnalysisQualityFixture fixture, AnalysisPort.AnalysisDraft draft) {
		assertThat(draft.participantSummary()).isNotBlank();
		assertThat(draft.summary()).isNotBlank();
		assertThat(draft.currentState()).isNotBlank();
		assertThat(draft.warnings()).isNotEmpty();
		assertThat(draft.primaryReply()).isNotBlank();
		assertThat(draft.alternativeReplies()).hasSizeGreaterThanOrEqualTo(2);
		assertThat(draft.replyReason()).isNotBlank();
		assertThat(draft.nextAction()).isNotBlank();
		assertThat(draft.summary()).doesNotContain(fixture.rawInput());
		assertThat(draft.primaryReply()).doesNotContain(fixture.forbiddenReplyTerms().toArray(String[]::new));
		assertThat(String.join(" ", draft.alternativeReplies()))
				.doesNotContain("몰래", "강제로", "죄책감", "무조건 고백", "성관계", "하룻밤");
	}
}
