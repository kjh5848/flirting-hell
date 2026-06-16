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
		// 연애 성향 5축 점수. 백엔드는 의미를 해석하지 않고 클라이언트가 보낸 JSON
		// 문자열을 그대로 보관한다(프레임워크가 바뀌어도 스키마 불변). nullable.
		String personalitySelf,
		String personalityIdeal,
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
			String personalitySelf,
			String personalityIdeal,
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
				personalitySelf,
				personalityIdeal,
				createdAt,
				updatedAt
		);
	}
}
