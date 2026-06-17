package com.flirtinghell.analysis.adapter.in.web;

import com.flirtinghell.analysis.application.port.out.AnalysisPort;
import com.flirtinghell.analysis.application.service.AnalysisService;
import com.flirtinghell.shared.api.ApiResponse;
import com.flirtinghell.shared.api.RequestIds;
import com.flirtinghell.shared.security.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/// 데이트 플랜 제안(비영속). 코스 + 식습관·취향 기반 궁합 확인 포인트를 함께 준다.
@RestController
@RequestMapping("/api/rooms/{roomId}/plan")
public class PlanController {

	private final AnalysisService analysisService;

	public PlanController(AnalysisService analysisService) {
		this.analysisService = analysisService;
	}

	@GetMapping
	ApiResponse<PlanResponse> plan(
			@AuthenticationPrincipal AuthenticatedUser user,
			@PathVariable String roomId,
			HttpServletRequest request
	) {
		AnalysisPort.PlanDraft plan = analysisService.suggestPlan(user.firebaseUid(), roomId);
		return ApiResponse.of(new PlanResponse(plan), RequestIds.from(request));
	}

	public record PlanResponse(AnalysisPort.PlanDraft plan) {
	}
}
