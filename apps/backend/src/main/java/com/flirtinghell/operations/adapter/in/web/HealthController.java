package com.flirtinghell.operations.adapter.in.web;

import com.flirtinghell.shared.api.ApiResponse;
import com.flirtinghell.shared.api.RequestIds;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

	@GetMapping
	ApiResponse<HealthResponse> health(HttpServletRequest request) {
		HealthResponse response = new HealthResponse("ok", "flirting-hell-api", "0.2.0");
		return ApiResponse.of(response, RequestIds.from(request));
	}

	public record HealthResponse(
			String status,
			String service,
			String version
	) {
	}
}
