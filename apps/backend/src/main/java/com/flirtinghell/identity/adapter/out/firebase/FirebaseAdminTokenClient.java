package com.flirtinghell.identity.adapter.out.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("firebase")
class FirebaseAdminTokenClient implements FirebaseTokenClient {

	private final FirebaseAuth firebaseAuth;

	FirebaseAdminTokenClient(FirebaseAuth firebaseAuth) {
		this.firebaseAuth = firebaseAuth;
	}

	@Override
	public String verifyIdToken(String token) {
		try {
			return firebaseAuth.verifyIdToken(token).getUid();
		} catch (FirebaseAuthException exception) {
			throw new IllegalArgumentException("Firebase token verification failed.", exception);
		}
	}
}
