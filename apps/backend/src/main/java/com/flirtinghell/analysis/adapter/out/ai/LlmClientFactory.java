package com.flirtinghell.analysis.adapter.out.ai;

import java.net.http.HttpClient;
import java.time.Duration;

import com.fasterxml.jackson.databind.ObjectMapper;

class LlmClientFactory {

	private final ObjectMapper objectMapper;
	private final HttpClient httpClient;

	LlmClientFactory(ObjectMapper objectMapper) {
		this(objectMapper, HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build());
	}

	LlmClientFactory(ObjectMapper objectMapper, HttpClient httpClient) {
		this.objectMapper = objectMapper;
		this.httpClient = httpClient;
	}

	LlmClient create(LlmProviderProperties properties) {
		return switch (properties.provider()) {
			case GPT -> new GptLlmClient(objectMapper, httpClient, properties);
			case GEMINI -> new GeminiLlmClient(objectMapper, httpClient, properties);
			case CLAUDE -> new ClaudeLlmClient(objectMapper, httpClient, properties);
		};
	}
}
