package com.flirtinghell.identity.application.port.out;

import com.flirtinghell.shared.security.AuthenticatedUser;

public interface FirebaseTokenVerifier {

	AuthenticatedUser verify(String token);
}
