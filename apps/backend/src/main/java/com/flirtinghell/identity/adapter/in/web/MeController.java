package com.flirtinghell.identity.adapter.in.web;

import com.flirtinghell.identity.application.service.UserBootstrapService;
import com.flirtinghell.profile.domain.model.GuidanceLevel;
import com.flirtinghell.shared.api.ApiResponse;
import com.flirtinghell.shared.api.RequestIds;
import com.flirtinghell.shared.security.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
public class MeController {

	private final UserBootstrapService userBootstrapService;

	public MeController(UserBootstrapService userBootstrapService) {
		this.userBootstrapService = userBootstrapService;
	}

	@GetMapping("/bootstrap")
	ApiResponse<UserBootstrapService.BootstrapResult> bootstrap(
			@AuthenticationPrincipal AuthenticatedUser user,
			HttpServletRequest request
	) {
		return ApiResponse.of(userBootstrapService.bootstrap(user.firebaseUid()), RequestIds.from(request));
	}

	@PatchMapping("/profile")
	ApiResponse<UpdateProfileResponse> updateProfile(
			@AuthenticationPrincipal AuthenticatedUser user,
			@Valid @RequestBody UpdateProfileRequest body,
			HttpServletRequest request
	) {
		UserBootstrapService.ProfileResult profile = userBootstrapService.updateProfile(
				user.firebaseUid(),
				body.toCommand()
		);
		return ApiResponse.of(new UpdateProfileResponse(profile), RequestIds.from(request));
	}

	public record UpdateProfileRequest(
			@Size(max = 40) String nickname,
			@Size(max = 120) String speechStyle,
			@Size(max = 120) String datingStyle,
			@NotNull GuidanceLevel guidanceLevel,
			@Size(max = 160) String preferredPartnerStyle,
			@Size(max = 160) String avoidAdvice
	) {
		UserBootstrapService.UpdateProfileCommand toCommand() {
			return new UserBootstrapService.UpdateProfileCommand(
					nickname,
					speechStyle,
					datingStyle,
					guidanceLevel,
					preferredPartnerStyle,
					avoidAdvice
			);
		}
	}

	public record UpdateProfileResponse(UserBootstrapService.ProfileResult profile) {
	}
}
