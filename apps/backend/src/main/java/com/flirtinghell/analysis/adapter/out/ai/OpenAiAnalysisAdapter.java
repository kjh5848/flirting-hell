package com.flirtinghell.analysis.adapter.out.ai;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flirtinghell.analysis.application.port.out.AnalysisPort;
import com.flirtinghell.analysis.domain.model.InputSourceType;
import com.flirtinghell.consultation.domain.model.StrategyId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "flirting-hell.ai.provider", havingValue = "openai")
class OpenAiAnalysisAdapter implements AnalysisPort {

	private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(25);
	private static final String SYSTEM_INSTRUCTIONS = """
			너는 플러팅지옥의 연애 대화 코치다.
			목표는 사용자의 불안을 줄이고, 상대를 존중하면서 대화를 자연스럽게 이어가도록 돕는 것이다.
			원본 대화 전문은 저장하지 않는다. 분석 결과에는 요약, 상태, 답장 후보, 피해야 할 말만 담는다.
			스토킹, 속임수, 죄책감 유발, 성적 압박, 강요, 집착을 조장하지 않는다.
			상대의 마음을 단정하지 말고 가능성과 근거, 다음 행동을 조심스럽게 제안한다.
			사용자의 말투를 과하게 바꾸지 말고, 붙여넣은 대화의 톤을 자연스럽게 따른다.
			""";

	private final ObjectMapper objectMapper;
	private final HttpClient httpClient;
	private final OpenAiAnalysisProperties properties;

	@Autowired
	OpenAiAnalysisAdapter(
			ObjectMapper objectMapper,
			@Value("${flirting-hell.ai.openai.base-url:https://api.openai.com/v1}") String baseUrl,
			@Value("${flirting-hell.ai.openai.api-key:}") String apiKey,
			@Value("${flirting-hell.ai.openai.model:gpt-4o-mini}") String model
	) {
		this(objectMapper, HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build(),
				new OpenAiAnalysisProperties("openai", URI.create(baseUrl), apiKey, model));
	}

	OpenAiAnalysisAdapter(ObjectMapper objectMapper, OpenAiAnalysisProperties properties) {
		this(objectMapper, HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build(), properties);
	}

	private OpenAiAnalysisAdapter(
			ObjectMapper objectMapper,
			HttpClient httpClient,
			OpenAiAnalysisProperties properties
	) {
		this.objectMapper = objectMapper;
		this.httpClient = httpClient;
		this.properties = properties;
	}

	@Override
	public AnalysisDraft analyze(AnalysisRequest request) {
		if (properties.apiKey() == null || properties.apiKey().isBlank()) {
			throw new IllegalStateException("OpenAI API key is required when flirting-hell.ai.provider=openai.");
		}
		try {
			HttpRequest httpRequest = HttpRequest.newBuilder()
					.uri(properties.baseUrl().resolve("/v1/responses"))
					.timeout(REQUEST_TIMEOUT)
					.header("Authorization", "Bearer " + properties.apiKey())
					.header("Content-Type", "application/json")
					.header("Accept", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody(request))))
					.build();
			HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() < 200 || response.statusCode() >= 300) {
				throw new IllegalStateException("OpenAI analysis request failed with status " + response.statusCode() + ".");
			}
			return parseDraft(response.body());
		} catch (IOException exception) {
			throw new IllegalStateException("OpenAI analysis request failed.", exception);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("OpenAI analysis request was interrupted.", exception);
		}
	}

	private Map<String, Object> requestBody(AnalysisRequest request) {
		return Map.of(
				"model", properties.model(),
				"input", List.of(
						Map.of("role", "system", "content", SYSTEM_INSTRUCTIONS),
						Map.of("role", "user", "content", userPrompt(request))
				),
				"text", Map.of(
						"format", Map.of(
								"type", "json_schema",
								"name", "flirting_hell_analysis",
								"strict", true,
								"schema", responseSchema()
						)
				)
		);
	}

	private String userPrompt(AnalysisRequest request) {
		return """
				상담방 정보:
				- 상대 별칭: %s
				- 관계 단계: %s
				- 현재 고민: %s
				- 조심할 점: %s
				- 사용자가 고른 전략: %s

				붙여넣은 대화/상황:
				%s

				반드시 JSON Schema에 맞춰 답하라. 원본 대화 전문은 저장하지 않는다.
				""".formatted(
				request.roomAlias(),
				request.relationshipStage(),
				blankToFallback(request.currentConcern()),
				blankToFallback(request.cautionNotes()),
				request.requestedStrategyId(),
				request.rawInput()
		);
	}

	private String blankToFallback(String value) {
		if (value == null || value.isBlank()) {
			return "없음";
		}
		return value;
	}

	private Map<String, Object> responseSchema() {
		Map<String, Object> stringField = Map.of("type", "string");
		Map<String, Object> stringArray = Map.of("type", "array", "items", stringField);
		return Map.of(
				"type", "object",
				"additionalProperties", false,
				"required", List.of(
						"sourceType",
						"participantSummary",
						"summary",
						"currentState",
						"recommendedStrategyId",
						"warnings",
						"primaryReply",
						"alternativeReplies",
						"replyReason",
						"nextAction"
				),
				"properties", Map.of(
						"sourceType", Map.of("type", "string", "enum", enumNames(InputSourceType.values())),
						"participantSummary", stringField,
						"summary", stringField,
						"currentState", stringField,
						"recommendedStrategyId", Map.of("type", "string", "enum", enumNames(StrategyId.values())),
						"warnings", stringArray,
						"primaryReply", stringField,
						"alternativeReplies", stringArray,
						"replyReason", stringField,
						"nextAction", stringField
				)
		);
	}

	private List<String> enumNames(Enum<?>[] values) {
		return java.util.Arrays.stream(values)
				.map(Enum::name)
				.toList();
	}

	private AnalysisDraft parseDraft(String responseBody) throws JsonProcessingException {
		String outputText = extractOutputText(objectMapper.readTree(responseBody));
		OpenAiAnalysisResponse response = objectMapper.readValue(outputText, OpenAiAnalysisResponse.class);
		return new AnalysisDraft(
				response.sourceType(),
				response.participantSummary(),
				response.summary(),
				response.currentState(),
				response.recommendedStrategyId(),
				response.warnings(),
				response.primaryReply(),
				response.alternativeReplies(),
				response.replyReason(),
				response.nextAction()
		);
	}

	private String extractOutputText(JsonNode root) {
		JsonNode outputText = root.path("output_text");
		if (outputText.isTextual()) {
			return outputText.asText();
		}
		for (JsonNode output : root.path("output")) {
			for (JsonNode content : output.path("content")) {
				if ("refusal".equals(content.path("type").asText())) {
					throw new IllegalStateException("OpenAI refused the analysis request.");
				}
				JsonNode text = content.path("text");
				if (text.isTextual()) {
					return text.asText();
				}
			}
		}
		throw new IllegalStateException("OpenAI response did not include output text.");
	}

	private record OpenAiAnalysisResponse(
			InputSourceType sourceType,
			String participantSummary,
			String summary,
			String currentState,
			StrategyId recommendedStrategyId,
			List<String> warnings,
			String primaryReply,
			List<String> alternativeReplies,
			String replyReason,
			String nextAction
	) {
	}
}
