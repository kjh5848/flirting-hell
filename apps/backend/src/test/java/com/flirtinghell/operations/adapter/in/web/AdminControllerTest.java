package com.flirtinghell.operations.adapter.in.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.http.MediaType;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void adminMetricsRequireAuthentication() throws Exception {
		mockMvc.perform(get("/api/admin/metrics"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void nonAdminUserIsForbidden() throws Exception {
		mockMvc.perform(get("/api/admin/metrics")
						.header("Authorization", "Bearer dev:user-1"))
				.andExpect(status().isForbidden());
	}

	@Test
	void adminCanReadMetricsAndUsers() throws Exception {
		mockMvc.perform(get("/api/admin/metrics")
						.header("Authorization", "Bearer dev:admin-1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.dailyActiveUsers").exists())
				.andExpect(jsonPath("$.data.outcome.sentGood").exists());

		mockMvc.perform(get("/api/admin/users")
						.header("Authorization", "Bearer dev:admin-1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.users[0].userId").exists());
	}

	@Test
	void moderationFlagsSeededUnsafeOutputs() throws Exception {
		mockMvc.perform(get("/api/admin/moderation")
						.header("Authorization", "Bearer dev:admin-1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.flags[0].rules[0]").exists());
	}

	@Test
	void moderationRequiresAdmin() throws Exception {
		mockMvc.perform(get("/api/admin/moderation")
						.header("Authorization", "Bearer dev:user-1"))
				.andExpect(status().isForbidden());
	}

	@Test
	void llmStatusReturnsProvider() throws Exception {
		mockMvc.perform(get("/api/admin/llm")
						.header("Authorization", "Bearer dev:admin-1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.provider").exists());
	}

	@Test
	void featureFlagCanBeToggled() throws Exception {
		mockMvc.perform(patch("/api/admin/flags/real_place_recommendation")
						.header("Authorization", "Bearer dev:admin-1")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"enabled\":true}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[0].key").exists());
	}

	@Test
	void flagsRequireAdmin() throws Exception {
		mockMvc.perform(get("/api/admin/flags")
						.header("Authorization", "Bearer dev:user-1"))
				.andExpect(status().isForbidden());
	}

	@Test
	void revenueReturnsMockMetrics() throws Exception {
		mockMvc.perform(get("/api/admin/revenue")
						.header("Authorization", "Bearer dev:admin-1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.mrrKrw").exists())
				.andExpect(jsonPath("$.data.byPackage[0].name").exists());
	}
}
