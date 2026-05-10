package com.flirtinghell.analysis.adapter.out.ai;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class GeminiLlmClient implements LlmClient {

	private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(25);

	private final ObjectMapper objectMapper;
	private final HttpClient httpClient;
	private final LlmProviderProperties properties;

	GeminiLlmClient(ObjectMapper objectMapper, HttpClient httpClient, LlmProviderProperties properties) {
		this.objectMapper = objectMapper;
		this.httpClient = httpClient;
		this.properties = properties;
	}

	@Override
	public String generateJson(LlmPrompt prompt) {
		try {
			HttpRequest request = HttpRequest.newBuilder()
					.uri(generateContentUri())
					.timeout(REQUEST_TIMEOUT)
					.header("x-goog-api-key", properties.requireApiKey())
					.header("Content-Type", "application/json")
					.header("Accept", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody(prompt))))
					.build();
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			validateStatus(response);
			return extractText(objectMapper.readTree(response.body()));
		} catch (IOException exception) {
			throw new IllegalStateException("Gemini analysis request failed.", exception);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Gemini analysis request was interrupted.", exception);
		}
	}

	private URI generateContentUri() {
		return properties.baseUrl().resolve("/v1beta/models/" + properties.model() + ":generateContent");
	}

	private Map<String, Object> requestBody(LlmPrompt prompt) {
		return Map.of(
				"systemInstruction", Map.of("parts", List.of(Map.of("text", prompt.systemInstructions()))),
				"contents", List.of(Map.of("parts", List.of(Map.of("text", prompt.userPrompt())))),
				"generationConfig", Map.of(
						"responseFormat", Map.of(
								"text", Map.of(
										"mimeType", "application/json",
										"schema", prompt.responseSchema()
								)
						)
				)
		);
	}

	private void validateStatus(HttpResponse<String> response) {
		if (response.statusCode() < 200 || response.statusCode() >= 300) {
			throw new IllegalStateException("Gemini analysis request failed with status " + response.statusCode() + ".");
		}
	}

	private String extractText(JsonNode root) {
		for (JsonNode candidate : root.path("candidates")) {
			for (JsonNode part : candidate.path("content").path("parts")) {
				JsonNode text = part.path("text");
				if (text.isTextual()) {
					return text.asText();
				}
			}
		}
		throw new IllegalStateException("Gemini response did not include output text.");
	}
}
