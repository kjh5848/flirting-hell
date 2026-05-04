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

		return new AuthenticatedUser(token.substring(DEV_TOKEN_PREFIX.length()));
	}
}
