package com.flirtinghell.operations.adapter.in.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
}
