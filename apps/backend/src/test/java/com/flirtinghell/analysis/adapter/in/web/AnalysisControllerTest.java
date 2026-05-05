package com.flirtinghell.analysis.adapter.in.web;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AnalysisControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void pastedConversationCreatesMockAnalysisTurnInsideRoomHistory() throws Exception {
		String authorization = "Bearer dev:analysis-user";
		String createRoomBody = """
				{
				  "alias": "지우",
				  "relationshipStage": "TALKING",
				  "currentConcern": "대화가 이어지는지 확인",
				  "cautionNotes": "추궁처럼 보이는 말은 피하기",
				  "preferredStrategyId": "DEVELOP_ROMANCE"
				}
				""";
		MvcResult roomResult = mockMvc.perform(post("/api/rooms")
						.header("Authorization", authorization)
						.contentType(MediaType.APPLICATION_JSON)
						.content(createRoomBody))
				.andExpect(status().isCreated())
				.andReturn();
		String roomId = JsonPath.read(roomResult.getResponse().getContentAsString(), "$.data.room.roomId");

		String analysisBody = """
				{
				  "rawInput": "나: 오늘 뭐해?\\n상대: 그냥 집에 있어 ㅋㅋ\\n나: 오 쉬는 날이네. 뭐하면서 쉬어?",
				  "requestedStrategyId": "MAKE_PLAN"
				}
				""";
		MvcResult analysisResult = mockMvc.perform(post("/api/rooms/{roomId}/analyses", roomId)
						.header("Authorization", authorization)
						.contentType(MediaType.APPLICATION_JSON)
						.content(analysisBody))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.turn.turnId", startsWith("turn_")))
				.andExpect(jsonPath("$.data.turn.sourceType").value("KAKAO"))
				.andExpect(jsonPath("$.data.turn.summary").value("상대가 집에서 쉬는 흐름이라 가볍게 대화를 이어갈 수 있어요."))
				.andExpect(jsonPath("$.data.turn.recommendedStrategyId").value("MAKE_PLAN"))
				.andExpect(jsonPath("$.data.turn.primaryReply").value("오 좋다 ㅋㅋ 그럼 오늘은 집에서 충전하는 날이네. 저녁은 뭐 먹을 생각이야?"))
				.andExpect(jsonPath("$.data.turn.alternativeReplies", hasSize(2)))
				.andExpect(jsonPath("$.data.turn.warnings[0]").value("지금 바로 마음을 확인하려고 몰아붙이지 마세요."))
				.andReturn();
		String turnId = JsonPath.read(analysisResult.getResponse().getContentAsString(), "$.data.turn.turnId");

		mockMvc.perform(get("/api/rooms/{roomId}", roomId)
						.header("Authorization", authorization))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.room.roomId").value(roomId))
				.andExpect(jsonPath("$.data.recentTurns[0].turnId").value(turnId))
				.andExpect(jsonPath("$.data.recentTurns[0].primaryReply").value("오 좋다 ㅋㅋ 그럼 오늘은 집에서 충전하는 날이네. 저녁은 뭐 먹을 생각이야?"));

		mockMvc.perform(get("/api/rooms")
						.header("Authorization", authorization))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.rooms[0].lastTurnSummary").value("상대가 집에서 쉬는 흐름이라 가볍게 대화를 이어갈 수 있어요."))
				.andExpect(jsonPath("$.data.rooms[0].savedReplyCount").value(1));
	}
}
