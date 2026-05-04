package com.flirtinghell.profile.domain.model;

import java.time.Instant;

public record UserProfile(
		String userId,
		String nickname,
		String speechStyle,
		String datingStyle,
		GuidanceLevel guidanceLevel,
		String preferredPartnerStyle,
		String avoidAdvice,
		Instant createdAt,
		Instant updatedAt
) {
	public UserProfile update(
			String nickname,
			String speechStyle,
			String datingStyle,
			GuidanceLevel guidanceLevel,
			String preferredPartnerStyle,
			String avoidAdvice,
			Instant updatedAt
	) {
		return new UserProfile(
				userId,
				nickname,
				speechStyle,
				datingStyle,
				guidanceLevel,
				preferredPartnerStyle,
				avoidAdvice,
				createdAt,
				updatedAt
		);
	}
}
