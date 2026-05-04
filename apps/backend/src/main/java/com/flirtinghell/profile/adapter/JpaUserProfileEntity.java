package com.flirtinghell.profile.adapter;

import java.time.Instant;

import com.flirtinghell.profile.domain.model.GuidanceLevel;
import com.flirtinghell.profile.domain.model.UserProfile;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_profiles")
class JpaUserProfileEntity {

	@Id
	@Column(name = "user_id")
	private String userId;

	private String nickname;

	@Column(name = "speech_style")
	private String speechStyle;

	@Column(name = "dating_style")
	private String datingStyle;

	@Enumerated(EnumType.STRING)
	@Column(name = "guidance_level", nullable = false)
	private GuidanceLevel guidanceLevel;

	@Column(name = "preferred_partner_style")
	private String preferredPartnerStyle;

	@Column(name = "avoid_advice")
	private String avoidAdvice;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected JpaUserProfileEntity() {
	}

	private JpaUserProfileEntity(
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
		this.userId = userId;
		this.nickname = nickname;
		this.speechStyle = speechStyle;
		this.datingStyle = datingStyle;
		this.guidanceLevel = guidanceLevel;
		this.preferredPartnerStyle = preferredPartnerStyle;
		this.avoidAdvice = avoidAdvice;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	static JpaUserProfileEntity fromDomain(UserProfile profile) {
		return new JpaUserProfileEntity(
				profile.userId(),
				profile.nickname(),
				profile.speechStyle(),
				profile.datingStyle(),
				profile.guidanceLevel(),
				profile.preferredPartnerStyle(),
				profile.avoidAdvice(),
				profile.createdAt(),
				profile.updatedAt()
		);
	}

	UserProfile toDomain() {
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
