package com.flirtinghell.identity.adapter.in.web;

import com.flirtinghell.shared.api.ApiResponse;
import com.flirtinghell.shared.api.RequestIds;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	@PostMapping("/kakao/exchange")
	@ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
	ApiResponse<KakaoExchangeResponse> exchangeKakaoToken(
			@Valid @RequestBody KakaoExchangeRequest body,
			HttpServletRequest request
	) {
		return ApiResponse.of(
				new KakaoExchangeResponse(null, false, "Kakao/Firebase Admin 연동 설정 후 활성화됩니다."),
				RequestIds.from(request)
		);
	}

	public record KakaoExchangeRequest(
			@NotBlank String kakaoAccessToken,
			DeviceRequest device
	) {
	}

	public record DeviceRequest(
			String platform,
			String appVersion
	) {
	}

	public record KakaoExchangeResponse(
			String firebaseCustomToken,
			boolean isNewUser,
			String setupRequired
	) {
	}
}
