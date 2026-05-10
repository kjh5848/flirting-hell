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

class ClaudeLlmClient implements LlmClient {

	private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(25);
	private static final String TOOL_NAME = "return_flirting_hell_analysis";

	private final ObjectMapper objectMapper;
	private final HttpClient httpClient;
	private final LlmProviderProperties properties;

	ClaudeLlmClient(ObjectMapper objectMapper, HttpClient httpClient, LlmProviderProperties properties) {
		this.objectMapper = objectMapper;
		this.httpClient = httpClient;
		this.properties = properties;
	}

	@Override
	public String generateJson(LlmPrompt prompt) {
		try {
			HttpRequest request = HttpRequest.newBuilder()
					.uri(properties.baseUrl().resolve("/v1/messages"))
					.timeout(REQUEST_TIMEOUT)
					.header("x-api-key", properties.requireApiKey())
					.header("anthropic-version", "2023-06-01")
					.header("Content-Type", "application/json")
					.header("Accept", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody(prompt))))
					.build();
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			validateStatus(response);
			return extractToolInput(objectMapper.readTree(response.body()));
		} catch (IOException exception) {
			throw new IllegalStateException("Claude analysis request failed.", exception);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Claude analysis request was interrupted.", exception);
		}
	}

	private Map<String, Object> requestBody(LlmPrompt prompt) {
		return Map.of(
				"model", properties.model(),
				"max_tokens", 1200,
				"system", prompt.systemInstructions(),
				"messages", List.of(Map.of("role", "user", "content", prompt.userPrompt())),
				"tools", List.of(Map.of(
						"name", TOOL_NAME,
						"description", "Return a structured Flirting Hell conversation analysis.",
						"input_schema", prompt.responseSchema()
				)),
				"tool_choice", Map.of("type", "tool", "name", TOOL_NAME)
		);
	}

	private void validateStatus(HttpResponse<String> response) {
		if (response.statusCode() < 200 || response.statusCode() >= 300) {
			throw new IllegalStateException("Claude analysis request failed with status " + response.statusCode() + ".");
		}
	}

	private String extractToolInput(JsonNode root) throws JsonProcessingException {
		for (JsonNode content : root.path("content")) {
			if ("tool_use".equals(content.path("type").asText()) && TOOL_NAME.equals(content.path("name").asText())) {
				JsonNode input = content.path("input");
				if (input.isObject()) {
					return objectMapper.writeValueAsString(input);
				}
			}
		}
		throw new IllegalStateException("Claude response did not include structured tool input.");
	}
}
