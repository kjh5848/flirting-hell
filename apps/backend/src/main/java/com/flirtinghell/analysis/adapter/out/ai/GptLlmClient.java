package com.flirtinghell.analysis.adapter.out.ai;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class GptLlmClient implements LlmClient {

	private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(25);

	private final ObjectMapper objectMapper;
	private final HttpClient httpClient;
	private final LlmProviderProperties properties;

	GptLlmClient(ObjectMapper objectMapper, HttpClient httpClient, LlmProviderProperties properties) {
		this.objectMapper = objectMapper;
		this.httpClient = httpClient;
		this.properties = properties;
	}

	@Override
	public String generateJson(LlmPrompt prompt) {
		try {
			HttpRequest request = HttpRequest.newBuilder()
					.uri(properties.baseUrl().resolve("/v1/responses"))
					.timeout(REQUEST_TIMEOUT)
					.header("Authorization", "Bearer " + properties.requireApiKey())
					.header("Content-Type", "application/json")
					.header("Accept", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody(prompt))))
					.build();
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			validateStatus(response, "GPT");
			return extractOutputText(objectMapper.readTree(response.body()));
		} catch (IOException exception) {
			throw new IllegalStateException("GPT analysis request failed.", exception);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("GPT analysis request was interrupted.", exception);
		}
	}

	private Map<String, Object> requestBody(LlmPrompt prompt) {
		return Map.of(
				"model", properties.model(),
				"input", List.of(
						Map.of("role", "system", "content", prompt.systemInstructions()),
						Map.of("role", "user", "content", prompt.userPrompt())
				),
				"text", Map.of(
						"format", Map.of(
								"type", "json_schema",
								"name", "flirting_hell_analysis",
								"strict", true,
								"schema", prompt.responseSchema()
						)
				)
		);
	}

	private void validateStatus(HttpResponse<String> response, String providerName) {
		if (response.statusCode() < 200 || response.statusCode() >= 300) {
			throw new IllegalStateException(providerName + " analysis request failed with status " + response.statusCode() + ".");
		}
	}

	private String extractOutputText(JsonNode root) throws JsonProcessingException {
		JsonNode outputText = root.path("output_text");
		if (outputText.isTextual()) {
			return outputText.asText();
		}
		for (JsonNode output : root.path("output")) {
			for (JsonNode content : output.path("content")) {
				if ("refusal".equals(content.path("type").asText())) {
					throw new IllegalStateException("GPT refused the analysis request.");
				}
				JsonNode text = content.path("text");
				if (text.isTextual()) {
					return text.asText();
				}
			}
		}
		throw new IllegalStateException("GPT response did not include output text.");
	}
}
