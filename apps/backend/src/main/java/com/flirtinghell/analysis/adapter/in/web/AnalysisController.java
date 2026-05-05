package com.flirtinghell.analysis.adapter.in.web;

import com.flirtinghell.analysis.application.service.AnalysisService;
import com.flirtinghell.consultation.domain.model.StrategyId;
import com.flirtinghell.shared.api.ApiResponse;
import com.flirtinghell.shared.api.RequestIds;
import com.flirtinghell.shared.security.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rooms/{roomId}/analyses")
public class AnalysisController {

	private final AnalysisService analysisService;

	public AnalysisController(AnalysisService analysisService) {
		this.analysisService = analysisService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	ApiResponse<CreateAnalysisResponse> createAnalysis(
			@AuthenticationPrincipal AuthenticatedUser user,
			@PathVariable String roomId,
			@Valid @RequestBody CreateAnalysisRequest body,
			HttpServletRequest request
	) {
		AnalysisService.AnalysisTurnResult turn = analysisService.createAnalysis(
				user.firebaseUid(),
				roomId,
				body.toCommand()
		);
		return ApiResponse.of(new CreateAnalysisResponse(turn), RequestIds.from(request));
	}

	public record CreateAnalysisRequest(
			@NotBlank @Size(max = 8000) String rawInput,
			StrategyId requestedStrategyId
	) {
		AnalysisService.CreateAnalysisCommand toCommand() {
			return new AnalysisService.CreateAnalysisCommand(rawInput, requestedStrategyId);
		}
	}

	public record CreateAnalysisResponse(AnalysisService.AnalysisTurnResult turn) {
	}
}
