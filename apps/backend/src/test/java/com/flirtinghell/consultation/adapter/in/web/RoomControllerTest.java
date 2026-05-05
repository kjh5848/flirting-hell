package com.flirtinghell.consultation.adapter.in.web;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RoomControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void createRoomPersistsForCurrentUserAndAppearsInListAndBootstrap() throws Exception {
		String authorization = "Bearer dev:rooms-create-user";
		String body = """
				{
				  "alias": "지우",
				  "relationshipStage": "TALKING",
				  "currentConcern": "상대 마음 확인",
				  "cautionNotes": "너무 빠른 고백은 피하기",
				  "preferredStrategyId": "DEVELOP_ROMANCE"
				}
				""";

		MvcResult result = mockMvc.perform(post("/api/rooms")
						.header("Authorization", authorization)
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.room.roomId", startsWith("room_")))
				.andExpect(jsonPath("$.data.room.alias").value("지우"))
				.andExpect(jsonPath("$.data.room.relationshipStage").value("TALKING"))
				.andExpect(jsonPath("$.data.room.currentConcern").value("상대 마음 확인"))
				.andExpect(jsonPath("$.data.room.cautionNotes").value("너무 빠른 고백은 피하기"))
				.andExpect(jsonPath("$.data.room.preferredStrategyId").value("DEVELOP_ROMANCE"))
				.andReturn();
		String roomId = JsonPath.read(result.getResponse().getContentAsString(), "$.data.room.roomId");

		mockMvc.perform(get("/api/rooms")
						.header("Authorization", authorization))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.rooms[0].roomId").value(roomId))
				.andExpect(jsonPath("$.data.rooms[0].alias").value("지우"));

		mockMvc.perform(get("/api/rooms/{roomId}", roomId)
						.header("Authorization", authorization))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.room.roomId").value(roomId))
				.andExpect(jsonPath("$.data.room.currentConcern").value("상대 마음 확인"))
				.andExpect(jsonPath("$.data.recentTurns").isArray())
				.andExpect(jsonPath("$.data.savedReplies").isArray());

		mockMvc.perform(get("/api/me/bootstrap")
						.header("Authorization", authorization))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.recentRooms[0].roomId").value(roomId))
				.andExpect(jsonPath("$.data.recentRooms[0].alias").value("지우"));
	}

	@Test
	void roomDetailIsNotVisibleToOtherUsers() throws Exception {
		MvcResult result = mockMvc.perform(post("/api/rooms")
						.header("Authorization", "Bearer dev:room-owner-user")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "alias": "하린",
								  "relationshipStage": "FIRST_CONTACT",
								  "currentConcern": "첫 연락을 어떻게 할지",
								  "cautionNotes": null,
								  "preferredStrategyId": "MAKE_PLAN"
								}
								"""))
				.andExpect(status().isCreated())
				.andReturn();
		String roomId = JsonPath.read(result.getResponse().getContentAsString(), "$.data.room.roomId");

		mockMvc.perform(get("/api/rooms/{roomId}", roomId)
						.header("Authorization", "Bearer dev:other-user"))
				.andExpect(status().isNotFound());
	}
}
