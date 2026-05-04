package com.flirtinghell.identity.adapter.out.firebase;

import com.flirtinghell.identity.application.port.out.FirebaseTokenVerifier;
import com.flirtinghell.shared.security.AuthenticatedUser;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

@Component
@Profile("firebase")
class FirebaseAdminTokenVerifier implements FirebaseTokenVerifier {

	private final FirebaseTokenClient firebaseTokenClient;

	FirebaseAdminTokenVerifier(FirebaseTokenClient firebaseTokenClient) {
		this.firebaseTokenClient = firebaseTokenClient;
	}

	@Override
	public AuthenticatedUser verify(String token) {
		if (token == null || token.isBlank()) {
			throw new BadCredentialsException("Invalid Firebase ID token.");
		}

		try {
			String firebaseUid = firebaseTokenClient.verifyIdToken(token);
			if (firebaseUid == null || firebaseUid.isBlank()) {
				throw new BadCredentialsException("Invalid Firebase ID token.");
			}
			return new AuthenticatedUser(firebaseUid);
		} catch (BadCredentialsException exception) {
			throw exception;
		} catch (RuntimeException exception) {
			throw new BadCredentialsException("Invalid Firebase ID token.", exception);
		}
	}
}
