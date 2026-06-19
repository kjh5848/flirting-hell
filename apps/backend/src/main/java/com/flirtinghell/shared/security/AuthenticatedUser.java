package com.flirtinghell.shared.security;

public record AuthenticatedUser(String firebaseUid, boolean admin) {

	public AuthenticatedUser(String firebaseUid) {
		this(firebaseUid, false);
	}
}
