package com.flirtinghell.identity.adapter.out.firebase;

import com.flirtinghell.shared.security.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FirebaseAdminTokenVerifierTest {

	@Test
	void returnsAuthenticatedUserFromVerifiedFirebaseUid() {
		FirebaseAdminTokenVerifier verifier = new FirebaseAdminTokenVerifier(token -> "firebase-uid-1");

		AuthenticatedUser user = verifier.verify("valid-token");

		assertThat(user.firebaseUid()).isEqualTo("firebase-uid-1");
	}

	@Test
	void rejectsBlankTokenBeforeCallingFirebase() {
		FirebaseAdminTokenVerifier verifier = new FirebaseAdminTokenVerifier(token -> "unused");

		assertThatThrownBy(() -> verifier.verify(" "))
				.isInstanceOf(BadCredentialsException.class);
	}

	@Test
	void wrapsFirebaseVerificationFailureAsBadCredentials() {
		FirebaseAdminTokenVerifier verifier = new FirebaseAdminTokenVerifier(token -> {
			throw new IllegalArgumentException("invalid token");
		});

		assertThatThrownBy(() -> verifier.verify("invalid-token"))
				.isInstanceOf(BadCredentialsException.class)
				.hasMessageContaining("Invalid Firebase ID token");
	}
}
