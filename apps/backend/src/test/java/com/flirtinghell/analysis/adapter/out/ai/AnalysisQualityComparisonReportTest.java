package com.flirtinghell.analysis.adapter.out.ai;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flirtinghell.analysis.application.port.out.AnalysisPort;
import com.flirtinghell.analysis.domain.model.InputSourceType;
import com.flirtinghell.consultation.domain.model.StrategyId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

class AnalysisQualityComparisonReportTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@TempDir
	private Path tempDir;

	@Test
	void writesMarkdownAndJsonReportWithoutRawInput() throws Exception {
		AnalysisQualityFixture fixture = AnalysisQualityFixtures.load(objectMapper).getFirst();
		AnalysisPort.AnalysisDraft draft = new AnalysisPort.AnalysisDraft(
				InputSourceType.KAKAO,
				"나와 상대 발화가 구분되어 있어요.",
				"상대가 집에서 쉬는 흐름이라 가볍게 이어가기 좋아요.",
				"호감 단정은 어렵지만 편하게 반응하는 상태예요.",
				StrategyId.MAKE_PLAN,
				List.of("지금 바로 마음을 확인하려고 몰아붙이지 마세요."),
				"오 넷플 좋지 ㅋㅋ 뭐 보고 있어?",
				List.of("집에서 쉬는 날 좋지 ㅋㅋ", "오늘은 충전 모드네 ㅋㅋ"),
				"상대가 말한 활동을 받아주면 부담이 적습니다.",
				"상대가 콘텐츠 취향을 말하면 가벼운 추천 교환으로 이어갑니다.",
				"{\"expression\":4,\"pace\":2,\"contact\":4,\"emotion\":3,\"values\":3,\"summary\":\"편하게 반응하는 유형\"}"
		);

		AnalysisQualityComparisonReport.ReportFiles files = AnalysisQualityComparisonReport.write(
				tempDir,
				LlmProvider.GEMINI,
				List.of(new AnalysisQualityComparisonReport.Result(fixture, draft, Duration.ofMillis(420)))
		);

		String markdown = Files.readString(files.markdownPath());
		String json = Files.readString(files.jsonPath());
		assertThat(markdown).contains("GEMINI", fixture.id(), draft.primaryReply(), "420ms");
		assertThat(markdown).doesNotContain(fixture.rawInput());
		assertThat(json).contains("\"provider\" : \"GEMINI\"", "\"fixtureId\" : \"" + fixture.id() + "\"");
		assertThat(json).doesNotContain(fixture.rawInput());
	}
}
