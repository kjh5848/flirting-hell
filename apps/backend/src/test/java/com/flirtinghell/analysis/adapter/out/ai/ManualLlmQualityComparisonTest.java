package com.flirtinghell.analysis.adapter.out.ai;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flirtinghell.analysis.application.port.out.AnalysisPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.assertj.core.api.Assertions.assertThat;

class ManualLlmQualityComparisonTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	@EnabledIfEnvironmentVariable(named = "RUN_LLM_QUALITY_COMPARISON", matches = "true")
	void writesRealProviderComparisonReport() {
		LlmProviderProperties properties = providerPropertiesFromEnvironment();
		LlmClient client = new LlmClientFactory(objectMapper).create(properties);
		LlmAnalysisAdapter adapter = new LlmAnalysisAdapter(objectMapper, client);
		List<AnalysisQualityComparisonReport.Result> results = new ArrayList<>();

		for (AnalysisQualityFixture fixture : AnalysisQualityFixtures.load(objectMapper)) {
			Instant startedAt = Instant.now();
			AnalysisPort.AnalysisDraft draft = adapter.analyze(fixture.toRequest());
			results.add(new AnalysisQualityComparisonReport.Result(
					fixture,
					draft,
					Duration.between(startedAt, Instant.now())
			));
		}

		AnalysisQualityComparisonReport.ReportFiles files = AnalysisQualityComparisonReport.write(
				java.nio.file.Path.of("build", "reports", "analysis-quality"),
				properties.provider(),
				results
		);

		assertThat(files.markdownPath()).exists();
		assertThat(files.jsonPath()).exists();
	}

	private static LlmProviderProperties providerPropertiesFromEnvironment() {
		LlmProvider provider = LlmProvider.from(requiredEnv("FLIRTING_HELL_AI_PROVIDER"));
		return switch (provider) {
			case GPT -> new LlmProviderProperties(
					provider,
					URI.create(envOrDefault("FLIRTING_HELL_GPT_BASE_URL", "https://api.openai.com/v1")),
					firstPresent("FLIRTING_HELL_GPT_API_KEY", "FLIRTING_HELL_OPENAI_API_KEY", "OPENAI_API_KEY"),
					envOrDefault("FLIRTING_HELL_GPT_MODEL", "gpt-4o-mini")
			);
			case GEMINI -> new LlmProviderProperties(
					provider,
					URI.create(envOrDefault("FLIRTING_HELL_GEMINI_BASE_URL", "https://generativelanguage.googleapis.com")),
					firstPresent("FLIRTING_HELL_GEMINI_API_KEY", "GEMINI_API_KEY"),
					envOrDefault("FLIRTING_HELL_GEMINI_MODEL", "gemini-2.5-flash-lite")
			);
			case CLAUDE -> new LlmProviderProperties(
					provider,
					URI.create(envOrDefault("FLIRTING_HELL_CLAUDE_BASE_URL", "https://api.anthropic.com")),
					firstPresent("FLIRTING_HELL_CLAUDE_API_KEY", "ANTHROPIC_API_KEY"),
					envOrDefault("FLIRTING_HELL_CLAUDE_MODEL", "claude-haiku-4-5-20251001")
			);
		};
	}

	private static String requiredEnv(String key) {
		String value = System.getenv(key);
		if (value == null || value.isBlank()) {
			throw new IllegalStateException(key + " is required when RUN_LLM_QUALITY_COMPARISON=true.");
		}
		return value;
	}

	private static String firstPresent(String... keys) {
		for (String key : keys) {
			String value = System.getenv(key);
			if (value != null && !value.isBlank()) {
				return value;
			}
		}
		throw new IllegalStateException(String.join(" or ", keys) + " is required when RUN_LLM_QUALITY_COMPARISON=true.");
	}

	private static String envOrDefault(String key, String fallback) {
		String value = System.getenv(key);
		if (value == null || value.isBlank()) {
			return fallback;
		}
		return value;
	}
}
