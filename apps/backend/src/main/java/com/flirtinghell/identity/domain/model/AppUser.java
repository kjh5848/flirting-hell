package com.flirtinghell.identity.domain.model;

import java.time.Instant;

public record AppUser(
		String id,
		String firebaseUid,
		AppUserStatus status,
		boolean onboardingCompleted,
		Instant createdAt,
		Instant updatedAt,
		Instant lastSeenAt
) {
}
