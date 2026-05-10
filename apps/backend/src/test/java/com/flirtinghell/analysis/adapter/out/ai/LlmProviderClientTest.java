package com.flirtinghell.analysis.adapter.out.ai;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LlmProviderClientTest {

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final HttpClient httpClient = HttpClient.newHttpClient();
	private HttpServer server;

	@AfterEach
	void tearDown() {
		if (server != null) {
			server.stop(0);
		}
	}

	@Test
	void gptClientUsesResponsesApiStructuredOutput() throws Exception {
		CapturedRequest captured = startServer("/v1/responses", gptResponse());
		GptLlmClient client = new GptLlmClient(objectMapper, httpClient, properties(
				LlmProvider.GPT,
				URI.create(serverBaseUrl() + "/v1"),
				"test-key",
				"gpt-4o-mini"
		));

		String json = client.generateJson(prompt());

		JsonNode request = objectMapper.readTree(captured.bodyText());
		assertThat(captured.pathText()).isEqualTo("/v1/responses");
		assertThat(captured.header("Authorization")).isEqualTo("Bearer test-key");
		assertThat(request.path("model").asText()).isEqualTo("gpt-4o-mini");
		assertThat(request.path("text").path("format").path("type").asText()).isEqualTo("json_schema");
		assertThat(json).contains("\"summary\"");
	}

	@Test
	void geminiClientUsesGenerateContentStructuredOutput() throws Exception {
		CapturedRequest captured = startServer("/v1beta/models/gemini-2.5-flash-lite:generateContent", geminiResponse());
		GeminiLlmClient client = new GeminiLlmClient(objectMapper, httpClient, properties(
				LlmProvider.GEMINI,
				URI.create(serverBaseUrl()),
				"test-key",
				"gemini-2.5-flash-lite"
		));

		String json = client.generateJson(prompt());

		JsonNode request = objectMapper.readTree(captured.bodyText());
		assertThat(captured.pathText()).isEqualTo("/v1beta/models/gemini-2.5-flash-lite:generateContent");
		assertThat(captured.header("x-goog-api-key")).isEqualTo("test-key");
		assertThat(request.path("generationConfig").path("responseFormat").path("text").path("mimeType").asText())
				.isEqualTo("application/json");
		assertThat(json).contains("\"summary\"");
	}

	@Test
	void claudeClientForcesToolUseForStructuredOutput() throws Exception {
		CapturedRequest captured = startServer("/v1/messages", claudeResponse());
		ClaudeLlmClient client = new ClaudeLlmClient(objectMapper, httpClient, properties(
				LlmProvider.CLAUDE,
				URI.create(serverBaseUrl()),
				"test-key",
				"claude-haiku-4-5-20251001"
		));

		String json = client.generateJson(prompt());

		JsonNode request = objectMapper.readTree(captured.bodyText());
		assertThat(captured.pathText()).isEqualTo("/v1/messages");
		assertThat(captured.header("x-api-key")).isEqualTo("test-key");
		assertThat(captured.header("anthropic-version")).isEqualTo("2023-06-01");
		assertThat(request.path("tool_choice").path("name").asText()).isEqualTo("return_flirting_hell_analysis");
		assertThat(request.path("tools").get(0).path("input_schema").path("type").asText()).isEqualTo("object");
		assertThat(json).contains("\"summary\"");
	}

	private CapturedRequest startServer(String path, String responseBody) throws IOException {
		AtomicReference<String> capturedPath = new AtomicReference<>();
		AtomicReference<com.sun.net.httpserver.Headers> capturedHeaders = new AtomicReference<>();
		AtomicReference<String> capturedBody = new AtomicReference<>();
		server = HttpServer.create(new InetSocketAddress(0), 0);
		server.createContext(path, exchange -> {
			capturedPath.set(exchange.getRequestURI().getPath());
			capturedHeaders.set(exchange.getRequestHeaders());
			capturedBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
			byte[] response = responseBody.getBytes(StandardCharsets.UTF_8);
			exchange.getResponseHeaders().add("Content-Type", "application/json");
			exchange.sendResponseHeaders(200, response.length);
			exchange.getResponseBody().write(response);
			exchange.close();
		});
		server.start();
		return new CapturedRequest(capturedPath, capturedHeaders, capturedBody);
	}

	private String serverBaseUrl() {
		return "http://127.0.0.1:" + server.getAddress().getPort();
	}

	private static LlmPrompt prompt() {
		return new LlmPrompt(
				"system instructions",
				"user prompt",
				Map.of(
						"type", "object",
						"required", java.util.List.of("summary"),
						"properties", Map.of("summary", Map.of("type", "string"))
				)
		);
	}

	private static LlmProviderProperties properties(
			LlmProvider provider,
			URI baseUrl,
			String apiKey,
			String model
	) {
		return new LlmProviderProperties(provider, baseUrl, apiKey, model);
	}

	private static String gptResponse() {
		return """
				{
				  "output": [
				    {
				      "content": [
				        {
				          "type": "output_text",
				          "text": "{\\"summary\\":\\"ok\\"}"
				        }
				      ]
				    }
				  ]
				}
				""";
	}

	private static String geminiResponse() {
		return """
				{
				  "candidates": [
				    {
				      "content": {
				        "parts": [
				          {"text": "{\\"summary\\":\\"ok\\"}"}
				        ]
				      }
				    }
				  ]
				}
				""";
	}

	private static String claudeResponse() {
		return """
				{
				  "content": [
				    {
				      "type": "tool_use",
				      "name": "return_flirting_hell_analysis",
				      "input": {"summary":"ok"}
				    }
				  ]
				}
				""";
	}

	private record CapturedRequest(
			AtomicReference<String> path,
			AtomicReference<com.sun.net.httpserver.Headers> headers,
			AtomicReference<String> body
	) {
		String header(String name) {
			return headers.get().getFirst(name);
		}

		String pathText() {
			return path.get();
		}

		String bodyText() {
			return body.get();
		}
	}
}
