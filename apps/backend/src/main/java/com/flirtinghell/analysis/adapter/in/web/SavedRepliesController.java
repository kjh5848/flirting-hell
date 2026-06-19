package com.flirtinghell.analysis.adapter.in.web;

import java.util.List;

import com.flirtinghell.analysis.application.service.AnalysisService;
import com.flirtinghell.shared.api.ApiResponse;
import com.flirtinghell.shared.api.RequestIds;
import com.flirtinghell.shared.security.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/// 사용자가 저장한 답장 목록(상대별). 원문이 아니라 요약·추천 답장만 모아 보여준다.
@RestController
@RequestMapping("/api/me/saved-replies")
public class SavedRepliesController {

	private final AnalysisService analysisService;

	public SavedRepliesController(AnalysisService analysisService) {
		this.analysisService = analysisService;
	}

	@GetMapping
	ApiResponse<SavedRepliesResponse> savedReplies(
			@AuthenticationPrincipal AuthenticatedUser user,
			HttpServletRequest request
	) {
		List<AnalysisService.SavedReplyResult> items =
				analysisService.listSavedReplies(user.firebaseUid());
		return ApiResponse.of(new SavedRepliesResponse(items), RequestIds.from(request));
	}

	public record SavedRepliesResponse(List<AnalysisService.SavedReplyResult> items) {
	}
}
