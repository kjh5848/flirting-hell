package com.flirtinghell.identity.adapter.out.firebase;

import com.flirtinghell.identity.application.port.out.FirebaseTokenVerifier;
import com.flirtinghell.shared.security.AuthenticatedUser;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

@Component
@Profile("!firebase")
class DevFirebaseTokenVerifier implements FirebaseTokenVerifier {

	private static final String DEV_TOKEN_PREFIX = "dev:";

	@Override
	public AuthenticatedUser verify(String token) {
		if (token == null || !token.startsWith(DEV_TOKEN_PREFIX) || token.length() == DEV_TOKEN_PREFIX.length()) {
			throw new BadCredentialsException("Invalid development Firebase token.");
		}

		String uid = token.substring(DEV_TOKEN_PREFIX.length());
		// dev 편의: uid가 admin으로 시작하면 어드민 권한을 준다(예: dev:admin-1).
		boolean admin = uid.startsWith("admin");
		return new AuthenticatedUser(uid, admin);
	}
}
