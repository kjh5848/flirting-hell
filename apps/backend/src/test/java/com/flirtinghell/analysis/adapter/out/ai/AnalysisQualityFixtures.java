package com.flirtinghell.analysis.adapter.out.ai;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

final class AnalysisQualityFixtures {

	private static final String RESOURCE_PATH = "analysis-fixtures/quality-fixtures.json";

	private AnalysisQualityFixtures() {
	}

	static List<AnalysisQualityFixture> load(ObjectMapper objectMapper) {
		try (InputStream stream = AnalysisQualityFixtures.class.getClassLoader().getResourceAsStream(RESOURCE_PATH)) {
			if (stream == null) {
				throw new IllegalStateException("Analysis quality fixture file is missing: " + RESOURCE_PATH);
			}
			List<AnalysisQualityFixture> fixtures = objectMapper.readValue(stream, new TypeReference<>() {
			});
			return List.copyOf(fixtures);
		} catch (IOException exception) {
			throw new IllegalStateException("Analysis quality fixture file is invalid: " + RESOURCE_PATH, exception);
		}
	}
}
