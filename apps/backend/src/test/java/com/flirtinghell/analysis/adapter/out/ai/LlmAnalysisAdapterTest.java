package com.flirtinghell.analysis.adapter.out.ai;

import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flirtinghell.analysis.application.port.out.AnalysisPort;
import com.flirtinghell.analysis.domain.model.InputSourceType;
import com.flirtinghell.consultation.domain.model.RelationshipStage;
import com.flirtinghell.consultation.domain.model.StrategyId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LlmAnalysisAdapterTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void buildsSafetyPromptAndMapsStructuredJsonToAnalysisDraft() throws Exception {
		AtomicReference<LlmPrompt> capturedPrompt = new AtomicReference<>();
		LlmClient client = prompt -> {
			capturedPrompt.set(prompt);
			return """
					{
					  "sourceType": "KAKAO",
					  "participantSummary": "나와 상대 발화가 구분되어 있어요.",
					  "summary": "상대가 집에서 쉬는 흐름이라 가볍게 대화를 이어갈 수 있어요.",
					  "currentState": "상대가 편하게 반응하는 중이라 부담 없이 이어갈 수 있어요.",
					  "recommendedStrategyId": "MAKE_PLAN",
					  "warnings": ["지금 바로 마음을 확인하려고 몰아붙이지 마세요."],
					  "primaryReply": "오 좋다 ㅋㅋ 그럼 오늘은 집에서 충전하는 날이네.",
					  "alternativeReplies": ["집에서 쉬는 날 좋지 ㅋㅋ", "무리하지 말고 쉬어 ㅋㅋ"],
					  "replyReason": "상대가 편한 상태를 말했으니 일상 질문이 안전합니다.",
					  "nextAction": "상대가 답하면 가벼운 선택지로 약속 가능성을 봅니다.",
					  "partnerType": {"expression": 4, "pace": 2, "contact": 4, "emotion": 3, "values": 3, "summary": "편하게 일상을 공유하는 유형"}
					}
					""";
		};
		LlmAnalysisAdapter adapter = new LlmAnalysisAdapter(objectMapper, client);

		AnalysisPort.AnalysisDraft draft = adapter.analyze(new AnalysisPort.AnalysisRequest(
				"지우",
				RelationshipStage.TALKING,
				"대화가 이어지는지 확인",
				"추궁처럼 보이는 말은 피하기",
				StrategyId.MAKE_PLAN,
				"나: 오늘 뭐해?\n상대: 그냥 집에 있어 ㅋㅋ",
				null,
				null,
				java.util.List.of(),
				null,
				null
		));

		LlmPrompt prompt = capturedPrompt.get();
		JsonNode schema = objectMapper.valueToTree(prompt.responseSchema());
		assertThat(prompt.systemInstructions()).contains("스토킹", "성적 압박", "상대의 마음을 단정하지 말고");
		assertThat(prompt.userPrompt()).contains("상대 별칭: 지우", "나: 오늘 뭐해?");
		assertThat(schema.path("required")).extracting(JsonNode::asText)
				.contains("primaryReply", "nextAction", "partnerType");
		assertThat(draft.sourceType()).isEqualTo(InputSourceType.KAKAO);
		assertThat(draft.recommendedStrategyId()).isEqualTo(StrategyId.MAKE_PLAN);
		assertThat(draft.primaryReply()).isEqualTo("오 좋다 ㅋㅋ 그럼 오늘은 집에서 충전하는 날이네.");
		JsonNode partnerType = objectMapper.readTree(draft.partnerType());
		assertThat(partnerType.path("pace").asInt()).isEqualTo(2);
		assertThat(partnerType.path("summary").asText()).isEqualTo("편하게 일상을 공유하는 유형");
	}
}
