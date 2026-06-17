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

	@Override
	public String refineReply(RefineRequest request) {
		try {
			String responseJson = client.generateJson(new LlmPrompt(
					SYSTEM_INSTRUCTIONS,
					refinePrompt(request),
					Map.of(
							"type", "object",
							"additionalProperties", false,
							"required", List.of("reply"),
							"properties", Map.of("reply", Map.of("type", "string"))
					)
			));
			return objectMapper.readTree(responseJson).get("reply").asText();
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("LLM refine response did not match the expected schema.", exception);
		}
	}

	@Override
	public PlanDraft suggestPlan(PlanRequest request) {
		try {
			String responseJson = client.generateJson(new LlmPrompt(
					SYSTEM_INSTRUCTIONS,
					planPrompt(request),
					planSchema()
			));
			LlmPlanResponse response = objectMapper.readValue(responseJson, LlmPlanResponse.class);
			return new PlanDraft(
					response.theme(),
					response.steps().stream()
							.map(step -> new PlanStep(step.title(), step.detail()))
							.toList(),
					response.checkPoints(),
					response.cautions()
			);
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("LLM plan response did not match the expected schema.", exception);
		}
	}

	private String planPrompt(PlanRequest request) {
		return """
				데이트 플랜을 제안하라. 코스(steps)뿐 아니라, 상대의 식습관·좋아하는/싫어하는 음식 같은
				취향을 자연스럽게 확인해 "나와 맞는지(궁합)"를 가늠할 확인 포인트(checkPoints)를 반드시 포함하라.

				관계 단계: %s
				현재 고민: %s
				상대 유형(5축 JSON, 없으면 없음): %s
				내가 원하는 이상형(5축 JSON, 없으면 없음): %s

				상대를 압박·조종하지 말고, 관계 단계에 맞는 부담 없는 코스를 제안하라.
				반드시 JSON Schema에 맞춰 답하라.
				""".formatted(
				request.relationshipStage(),
				blankToFallback(request.currentConcern()),
				blankToFallback(request.latestPartnerType()),
				blankToFallback(request.myPersonalityIdeal())
		);
	}

	private Map<String, Object> planSchema() {
		Map<String, Object> stringField = Map.of("type", "string");
		Map<String, Object> stringArray = Map.of("type", "array", "items", stringField);
		Map<String, Object> stepItem = Map.of(
				"type", "object",
				"additionalProperties", false,
				"required", List.of("title", "detail"),
				"properties", Map.of("title", stringField, "detail", stringField)
		);
		return Map.of(
				"type", "object",
				"additionalProperties", false,
				"required", List.of("theme", "steps", "checkPoints", "cautions"),
				"properties", Map.of(
						"theme", stringField,
						"steps", Map.of("type", "array", "items", stepItem),
						"checkPoints", stringArray,
						"cautions", stringArray
				)
		);
	}

	private record LlmPlanResponse(
			String theme,
			List<LlmPlanStep> steps,
			List<String> checkPoints,
			List<String> cautions
	) {
	}

	private record LlmPlanStep(String title, String detail) {
	}

	@Override
	public String coachReply(CoachRequest request) {
		try {
			String responseJson = client.generateJson(new LlmPrompt(
					SYSTEM_INSTRUCTIONS,
					coachPrompt(request),
					Map.of(
							"type", "object",
							"additionalProperties", false,
							"required", List.of("reply"),
							"properties", Map.of("reply", Map.of("type", "string"))
					)
			));
			return objectMapper.readTree(responseJson).get("reply").asText();
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("LLM coach response did not match the expected schema.", exception);
		}
	}

	private String coachPrompt(CoachRequest request) {
		StringBuilder history = new StringBuilder();
		for (AnalysisPort.CoachMessage message : request.history()) {
			history.append(message.role() == AnalysisPort.CoachRole.USER ? "사용자: " : "코치: ")
					.append(message.text())
					.append('\n');
		}
		return """
				너는 사용자의 연애 고민을 들어주고 코칭하는 대화 상대다. 먼저 공감하고, 그다음
				작은 질문이나 부담 없는 제안을 한다. 조종·압박·집착을 권하지 않는다.

				상담 맥락 - 현재 고민: %s
				상대 유형(5축 JSON, 없으면 없음): %s
				사용자가 원하는 이상형(5축 JSON, 없으면 없음): %s

				지금까지 대화:
				%s사용자: %s

				코치로서 다음 한 마디만 자연스럽게 답하라. JSON {"reply": "..."} 형식으로만 답하라.
				""".formatted(
				blankToFallback(request.roomConcern()),
				blankToFallback(request.latestPartnerType()),
				blankToFallback(request.myPersonalityIdeal()),
				history.toString(),
				request.userMessage()
		);
	}

	private String refinePrompt(RefineRequest request) {
		return """
				직전 추천 답장: %s
				상대 유형(5축 JSON, 없으면 없음): %s
				요청한 톤 방향: %s

				위 답장을 요청한 톤 방향으로 자연스럽게 다시 써라. 상대를 압박하거나 조종하지 말고,
				사용자의 말투를 과하게 바꾸지 마라. JSON {"reply": "..."} 형식으로만 답하라.
				""".formatted(
				request.previousReply(),
				blankToFallback(request.latestPartnerType()),
				refineDirectionLabel(request.direction())
		);
	}

	private String refineDirectionLabel(RefineDirection direction) {
		return switch (direction) {
			case LIGHTER -> "더 가볍고 편하게";
			case MORE_SERIOUS -> "더 진지하고 마음을 담아서";
			case SLOWER -> "부담을 줄이고 천천히 가는 톤으로";
			case BOLDER -> "조금 더 적극적으로(단, 압박은 금지)";
		};
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

				사용자(나) 성향(5축 JSON, 없으면 미설정): %s
				사용자가 원하는 이상형(5축 JSON, 없으면 미설정): %s
				직전 분석 요약(최근순, 없으면 없음):
				%s
				직전에 추론한 상대 유형(5축 JSON, 없으면 없음): %s

				붙여넣은 대화/상황:
				%s

				지침:
				- 위 직전 요약이 있으면 흐름을 이어서 코칭하라(처음인 척하지 말 것).
				- partnerType(상대 5축)을 추론하고, 내 성향과 상대 유형의 차이를 반영해 답장·다음 행동을 개인화하라.
				- 반드시 JSON Schema에 맞춰 답하라. 원본 대화 전문은 저장하지 않는다.
				""".formatted(
				request.roomAlias(),
				request.relationshipStage(),
				blankToFallback(request.currentConcern()),
				blankToFallback(request.cautionNotes()),
				request.requestedStrategyId(),
				blankToFallback(request.myPersonalitySelf()),
				blankToFallback(request.myPersonalityIdeal()),
				recentSummariesText(request.recentSummaries()),
				blankToFallback(request.latestPartnerType()),
				request.rawInput()
		);
	}

	private String recentSummariesText(List<String> summaries) {
		if (summaries == null || summaries.isEmpty()) {
			return "없음";
		}
		StringBuilder builder = new StringBuilder();
		for (String summary : summaries) {
			builder.append("- ").append(summary).append('\n');
		}
		return builder.toString().stripTrailing();
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
