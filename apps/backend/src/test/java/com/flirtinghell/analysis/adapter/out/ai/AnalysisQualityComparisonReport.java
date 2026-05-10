package com.flirtinghell.analysis.adapter.out.ai;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.flirtinghell.analysis.application.port.out.AnalysisPort;

final class AnalysisQualityComparisonReport {

	private static final ObjectMapper objectMapper = new ObjectMapper()
			.enable(SerializationFeature.INDENT_OUTPUT);

	private AnalysisQualityComparisonReport() {
	}

	static ReportFiles write(Path reportDirectory, LlmProvider provider, List<Result> results) {
		try {
			Files.createDirectories(reportDirectory);
			Path markdownPath = reportDirectory.resolve(provider.name().toLowerCase() + "-analysis-quality.md");
			Path jsonPath = reportDirectory.resolve(provider.name().toLowerCase() + "-analysis-quality.json");
			Files.writeString(markdownPath, markdown(provider, results));
			Files.writeString(jsonPath, json(provider, results));
			return new ReportFiles(markdownPath, jsonPath);
		} catch (IOException exception) {
			throw new IllegalStateException("Failed to write LLM quality comparison report.", exception);
		}
	}

	private static String markdown(LlmProvider provider, List<Result> results) {
		StringBuilder builder = new StringBuilder();
		builder.append("# LLM 품질 비교 리포트\n\n");
		builder.append("- Provider: `").append(provider.name()).append("`\n");
		builder.append("- Fixture count: `").append(results.size()).append("`\n");
		builder.append("- 원본 대화 전문은 리포트에 저장하지 않는다.\n\n");
		builder.append("| Fixture | Source | Strategy | Latency | Primary reply | Warnings | Next action |\n");
		builder.append("| --- | --- | --- | ---: | --- | --- | --- |\n");
		for (Result result : results) {
			AnalysisPort.AnalysisDraft draft = result.draft();
			builder.append("| `").append(escape(result.fixture().id())).append("` ")
					.append("| `").append(draft.sourceType()).append("` ")
					.append("| `").append(draft.recommendedStrategyId()).append("` ")
					.append("| ").append(result.latency().toMillis()).append("ms ")
					.append("| ").append(escape(draft.primaryReply())).append(" ")
					.append("| ").append(escape(String.join(" / ", draft.warnings()))).append(" ")
					.append("| ").append(escape(draft.nextAction())).append(" |\n");
		}
		return builder.toString();
	}

	private static String json(LlmProvider provider, List<Result> results) throws IOException {
		List<Map<String, Object>> items = results.stream()
				.map(AnalysisQualityComparisonReport::jsonItem)
				.toList();
		return objectMapper.writeValueAsString(Map.of(
				"provider", provider,
				"fixtureCount", results.size(),
				"results", items
		));
	}

	private static Map<String, Object> jsonItem(Result result) {
		Map<String, Object> item = new LinkedHashMap<>();
		item.put("fixtureId", result.fixture().id());
		item.put("title", result.fixture().title());
		item.put("latencyMs", result.latency().toMillis());
		item.put("sourceType", result.draft().sourceType());
		item.put("recommendedStrategyId", result.draft().recommendedStrategyId());
		item.put("summary", result.draft().summary());
		item.put("currentState", result.draft().currentState());
		item.put("warnings", result.draft().warnings());
		item.put("primaryReply", result.draft().primaryReply());
		item.put("alternativeReplies", result.draft().alternativeReplies());
		item.put("replyReason", result.draft().replyReason());
		item.put("nextAction", result.draft().nextAction());
		return item;
	}

	private static String escape(String value) {
		return value.replace("|", "\\|").replace("\n", " ");
	}

	record Result(
			AnalysisQualityFixture fixture,
			AnalysisPort.AnalysisDraft draft,
			Duration latency
	) {
	}

	record ReportFiles(
			Path markdownPath,
			Path jsonPath
	) {
	}
}
