package com.flirtinghell.identity.adapter.in.web;

import com.flirtinghell.shared.api.RequestIds;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MeControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void bootstrapRequiresAuthentication() throws Exception {
		mockMvc.perform(get("/api/me/bootstrap"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void bootstrapCreatesDevelopmentUserAndDefaultProfile() throws Exception {
		mockMvc.perform(get("/api/me/bootstrap")
						.header(RequestIds.HEADER_NAME, "req_bootstrap")
						.header("Authorization", "Bearer dev:firebase-test-user"))
				.andExpect(status().isOk())
				.andExpect(header().string(RequestIds.HEADER_NAME, "req_bootstrap"))
				.andExpect(jsonPath("$.data.user.userId", startsWith("usr_")))
				.andExpect(jsonPath("$.data.user.onboardingCompleted").value(false))
				.andExpect(jsonPath("$.data.user.profile.guidanceLevel").value("BALANCED"))
				.andExpect(jsonPath("$.data.usage.freeAnalyses.remaining").value(3))
				.andExpect(jsonPath("$.data.recentRooms").isArray());
	}

	@Test
	void updateProfileReturnsUpdatedProfile() throws Exception {
		String body = """
				{
				  "nickname": "주혁",
				  "speechStyle": "짧고 자연스럽게",
				  "datingStyle": "천천히 확인하는 편",
				  "guidanceLevel": "REALITY_CHECK",
				  "preferredPartnerStyle": "다정하게 표현하는 사람",
				  "avoidAdvice": "단정하지 않기"
				}
				""";

		mockMvc.perform(patch("/api/me/profile")
						.header("Authorization", "Bearer dev:firebase-profile-user")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.profile.nickname").value("주혁"))
				.andExpect(jsonPath("$.data.profile.guidanceLevel").value("REALITY_CHECK"))
				.andExpect(jsonPath("$.data.profile.avoidAdvice").value("단정하지 않기"));

		mockMvc.perform(get("/api/me/bootstrap")
						.header("Authorization", "Bearer dev:firebase-profile-user"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.user.onboardingCompleted").value(true));
	}
}
