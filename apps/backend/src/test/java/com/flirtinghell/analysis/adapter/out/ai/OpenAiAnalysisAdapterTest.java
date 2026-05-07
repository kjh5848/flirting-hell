package com.flirtinghell.analysis.adapter.out.ai;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flirtinghell.analysis.application.port.out.AnalysisPort;
import com.flirtinghell.analysis.domain.model.InputSourceType;
import com.flirtinghell.consultation.domain.model.RelationshipStage;
import com.flirtinghell.consultation.domain.model.StrategyId;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenAiAnalysisAdapterTest {

	private final ObjectMapper objectMapper = new ObjectMapper();
	private HttpServer server;

	@AfterEach
	void tearDown() {
		if (server != null) {
			server.stop(0);
		}
	}

	@Test
	void sendsResponsesApiStructuredOutputRequestAndMapsAnalysisDraft() throws Exception {
		AtomicReference<String> capturedPath = new AtomicReference<>();
		AtomicReference<String> capturedAuthorization = new AtomicReference<>();
		AtomicReference<String> capturedBody = new AtomicReference<>();
		server = HttpServer.create(new InetSocketAddress(0), 0);
		server.createContext("/v1/responses", exchange -> {
			capturedPath.set(exchange.getRequestURI().getPath());
			capturedAuthorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
			capturedBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
			byte[] response = openAiResponse().getBytes(StandardCharsets.UTF_8);
			exchange.getResponseHeaders().add("Content-Type", "application/json");
			exchange.sendResponseHeaders(200, response.length);
			exchange.getResponseBody().write(response);
			exchange.close();
		});
		server.start();

		OpenAiAnalysisAdapter adapter = new OpenAiAnalysisAdapter(
				objectMapper,
				new OpenAiAnalysisProperties(
						"openai",
						URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/v1"),
						"test-key",
						"gpt-4o-mini"
				)
		);

		AnalysisPort.AnalysisDraft draft = adapter.analyze(new AnalysisPort.AnalysisRequest(
				"지우",
				RelationshipStage.TALKING,
				"대화가 이어지는지 확인",
				"추궁처럼 보이는 말은 피하기",
				StrategyId.MAKE_PLAN,
				"나: 오늘 뭐해?\n상대: 그냥 집에 있어 ㅋㅋ"
		));

		JsonNode request = objectMapper.readTree(capturedBody.get());
		assertThat(capturedPath.get()).isEqualTo("/v1/responses");
		assertThat(capturedAuthorization.get()).isEqualTo("Bearer test-key");
		assertThat(request.path("model").asText()).isEqualTo("gpt-4o-mini");
		assertThat(request.path("text").path("format").path("type").asText()).isEqualTo("json_schema");
		assertThat(request.path("text").path("format").path("strict").asBoolean()).isTrue();
		assertThat(capturedBody.get()).contains("원본 대화 전문은 저장하지 않는다");
		assertThat(capturedBody.get()).contains("나: 오늘 뭐해?");
		assertThat(draft.sourceType()).isEqualTo(InputSourceType.KAKAO);
		assertThat(draft.recommendedStrategyId()).isEqualTo(StrategyId.MAKE_PLAN);
		assertThat(draft.primaryReply()).isEqualTo("오 좋다 ㅋㅋ 그럼 오늘은 집에서 충전하는 날이네. 저녁은 뭐 먹을 생각이야?");
		assertThat(draft.warnings()).contains("지금 바로 마음을 확인하려고 몰아붙이지 마세요.");
	}

	private static String openAiResponse() throws IOException {
		String content = """
				{
				  "sourceType": "KAKAO",
				  "participantSummary": "나와 상대 발화가 구분되어 있어요.",
				  "summary": "상대가 집에서 쉬는 흐름이라 가볍게 대화를 이어갈 수 있어요.",
				  "currentState": "상대가 편하게 반응하는 중이라 부담 없이 한 번 더 이어갈 수 있어요.",
				  "recommendedStrategyId": "MAKE_PLAN",
				  "warnings": [
				    "지금 바로 마음을 확인하려고 몰아붙이지 마세요.",
				    "답장이 늦어도 추궁처럼 보이는 표현은 피하세요."
				  ],
				  "primaryReply": "오 좋다 ㅋㅋ 그럼 오늘은 집에서 충전하는 날이네. 저녁은 뭐 먹을 생각이야?",
				  "alternativeReplies": [
				    "집에서 쉬는 날 좋지 ㅋㅋ 나는 이런 날 맛있는 거 먹으면 바로 회복돼",
				    "그럼 오늘은 무리하지 말고 쉬어 ㅋㅋ 나중에 컨디션 좋을 때 맛있는 거 먹자"
				  ],
				  "replyReason": "상대가 편한 상태를 말했으니 일상 질문으로 이어가는 편이 안전합니다.",
				  "nextAction": "상대가 답하면 가벼운 선택지로 약속 가능성을 봅니다."
				}
				""";
		return """
				{
				  "id": "resp_test",
				  "object": "response",
				  "status": "completed",
				  "output": [
				    {
				      "type": "message",
				      "role": "assistant",
				      "content": [
				        {
				          "type": "output_text",
				          "text": %s
				        }
				      ]
				    }
				  ]
				}
				""".formatted(new ObjectMapper().writeValueAsString(content));
	}
}
