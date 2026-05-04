package com.flirtinghell.identity.adapter.out.persistence;

import java.time.Instant;

import com.flirtinghell.identity.domain.model.AppUser;
import com.flirtinghell.identity.domain.model.AppUserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_users")
class JpaAppUserEntity {

	@Id
	private String id;

	@Column(name = "firebase_uid", nullable = false, unique = true)
	private String firebaseUid;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private AppUserStatus status;

	@Column(name = "onboarding_completed", nullable = false)
	private boolean onboardingCompleted;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "last_seen_at")
	private Instant lastSeenAt;

	protected JpaAppUserEntity() {
	}

	private JpaAppUserEntity(
			String id,
			String firebaseUid,
			AppUserStatus status,
			boolean onboardingCompleted,
			Instant createdAt,
			Instant updatedAt,
			Instant lastSeenAt
	) {
		this.id = id;
		this.firebaseUid = firebaseUid;
		this.status = status;
		this.onboardingCompleted = onboardingCompleted;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.lastSeenAt = lastSeenAt;
	}

	static JpaAppUserEntity fromDomain(AppUser user) {
		return new JpaAppUserEntity(
				user.id(),
				user.firebaseUid(),
				user.status(),
				user.onboardingCompleted(),
				user.createdAt(),
				user.updatedAt(),
				user.lastSeenAt()
		);
	}

	AppUser toDomain() {
		return new AppUser(
				id,
				firebaseUid,
				status,
				onboardingCompleted,
				createdAt,
				updatedAt,
				lastSeenAt
		);
	}
}
