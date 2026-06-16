package com.flirtinghell.analysis.adapter.out.ai;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flirtinghell.analysis.application.port.out.AnalysisPort;
import com.flirtinghell.analysis.domain.model.InputSourceType;
import com.flirtinghell.consultation.domain.model.StrategyId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnExpression("'${flirting-hell.ai.provider:mock}' != 'mock'")
class LlmAnalysisAdapter implements AnalysisPort {

	private static final String SYSTEM_INSTRUCTIONS = """
			너는 플러팅지옥의 연애 대화 코치다.
			목표는 사용자의 불안을 줄이고, 상대를 존중하면서 대화를 자연스럽게 이어가도록 돕는 것이다.
			원본 대화 전문은 저장하지 않는다. 분석 결과에는 요약, 상태, 답장 후보, 피해야 할 말만 담는다.
			스토킹, 속임수, 죄책감 유발, 성적 압박, 강요, 집착을 조장하지 않는다.
			상대의 마음을 단정하지 말고 가능성과 근거, 다음 행동을 조심스럽게 제안한다.
			사용자의 말투를 과하게 바꾸지 말고, 붙여넣은 대화의 톤을 자연스럽게 따른다.
			""";

	private final ObjectMapper objectMapper;
	private final LlmClient client;

	@Autowired
	LlmAnalysisAdapter(
			ObjectMapper objectMapper,
			@Value("${flirting-hell.ai.provider:mock}") String provider,
			@Value("${flirting-hell.ai.gpt.base-url:https://api.openai.com/v1}") String gptBaseUrl,
			@Value("${flirting-hell.ai.gpt.api-key:}") String gptApiKey,
			@Value("${flirting-hell.ai.gpt.model:gpt-4o-mini}") String gptModel,
			@Value("${flirting-hell.ai.gemini.base-url:https://generativelanguage.googleapis.com}") String geminiBaseUrl,
			@Value("${flirting-hell.ai.gemini.api-key:}") String geminiApiKey,
			@Value("${flirting-hell.ai.gemini.model:gemini-2.5-flash-lite}") String geminiModel,
			@Value("${flirting-hell.ai.claude.base-url:https://api.anthropic.com}") String claudeBaseUrl,
			@Value("${flirting-hell.ai.claude.api-key:}") String claudeApiKey,
			@Value("${flirting-hell.ai.claude.model:claude-haiku-4-5-20251001}") String claudeModel
	) {
		this(
				objectMapper,
				new LlmClientFactory(objectMapper).create(resolveProperties(
						provider,
						gptBaseUrl,
						gptApiKey,
						gptModel,
						geminiBaseUrl,
						geminiApiKey,
						geminiModel,
						claudeBaseUrl,
						claudeApiKey,
						claudeModel
				))
		);
	}

	LlmAnalysisAdapter(ObjectMapper objectMapper, LlmClient client) {
		this.objectMapper = objectMapper;
		this.client = client;
	}

	@Override
	public AnalysisDraft analyze(AnalysisRequest request) {
		try {
			String responseJson = client.generateJson(new LlmPrompt(
					SYSTEM_INSTRUCTIONS,
					userPrompt(request),
					responseSchema()
			));
			LlmAnalysisResponse response = objectMapper.readValue(responseJson, LlmAnalysisResponse.class);
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
					response.nextAction(),
					// TODO: 실제 LLM 응답 스키마에 partnerType(5축)을 추가해 채운다. 지금은 미추론.
					null
			);
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("LLM analysis response did not match the expected schema.", exception);
		}
	}

	private static LlmProviderProperties resolveProperties(
			String provider,
			String gptBaseUrl,
			String gptApiKey,
			String gptModel,
			String geminiBaseUrl,
			String geminiApiKey,
			String geminiModel,
			String claudeBaseUrl,
			String claudeApiKey,
			String claudeModel
	) {
		LlmProvider llmProvider = LlmProvider.from(provider);
		return switch (llmProvider) {
			case GPT -> new LlmProviderProperties(llmProvider, URI.create(gptBaseUrl), gptApiKey, gptModel);
			case GEMINI -> new LlmProviderProperties(llmProvider, URI.create(geminiBaseUrl), geminiApiKey, geminiModel);
			case CLAUDE -> new LlmProviderProperties(llmProvider, URI.create(claudeBaseUrl), claudeApiKey, claudeModel);
		};
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
		return Arrays.stream(values)
				.map(Enum::name)
				.toList();
	}

	private record LlmAnalysisResponse(
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
