package com.flirtinghell.operations.adapter.in.web;

import com.flirtinghell.shared.api.RequestIds;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class HealthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void healthReturnsApiResponseWithRequestId() throws Exception {
		mockMvc.perform(get("/api/health").header(RequestIds.HEADER_NAME, "req_test"))
				.andExpect(status().isOk())
				.andExpect(header().string(RequestIds.HEADER_NAME, "req_test"))
				.andExpect(jsonPath("$.data.status").value("ok"))
				.andExpect(jsonPath("$.data.service").value("flirting-hell-api"))
				.andExpect(jsonPath("$.meta.requestId").value("req_test"));
	}
}
