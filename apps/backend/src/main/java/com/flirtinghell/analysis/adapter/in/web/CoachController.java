package com.flirtinghell.analysis.adapter.in.web;

import java.util.List;

import com.flirtinghell.analysis.application.port.out.AnalysisPort;
import com.flirtinghell.analysis.application.service.AnalysisService;
import com.flirtinghell.shared.api.ApiResponse;
import com.flirtinghell.shared.api.RequestIds;
import com.flirtinghell.shared.security.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/// 상담방 안에서 코치와의 멀티턴 대화. 상태 비저장 — 히스토리는 클라이언트가 보낸다.
@RestController
@RequestMapping("/api/rooms/{roomId}/coach")
public class CoachController {

	private final AnalysisService analysisService;

	public CoachController(AnalysisService analysisService) {
		this.analysisService = analysisService;
	}

	@PostMapping
	ApiResponse<CoachResponse> coach(
			@AuthenticationPrincipal AuthenticatedUser user,
			@PathVariable String roomId,
			@Valid @RequestBody CoachRequestBody body,
			HttpServletRequest request
	) {
		AnalysisService.CoachReplyResult result = analysisService.coach(
				user.firebaseUid(),
				roomId,
				body.toCommand()
		);
		return ApiResponse.of(new CoachResponse(result.reply()), RequestIds.from(request));
	}

	public record CoachRequestBody(
			@Valid List<MessageDto> history,
			@NotBlank @Size(max = 2000) String userMessage
	) {
		AnalysisService.CoachCommand toCommand() {
			List<AnalysisPort.CoachMessage> messages = history == null
					? List.of()
					: history.stream().map(MessageDto::toMessage).toList();
			return new AnalysisService.CoachCommand(messages, userMessage);
		}
	}

	public record MessageDto(
			@NotNull AnalysisPort.CoachRole role,
			@NotBlank @Size(max = 2000) String text
	) {
		AnalysisPort.CoachMessage toMessage() {
			return new AnalysisPort.CoachMessage(role, text);
		}
	}

	public record CoachResponse(String reply) {
	}
}
