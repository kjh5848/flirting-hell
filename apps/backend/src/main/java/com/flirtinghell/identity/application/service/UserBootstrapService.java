package com.flirtinghell.identity.application.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import com.flirtinghell.identity.domain.model.AppUser;
import com.flirtinghell.identity.domain.model.AppUserStatus;
import com.flirtinghell.identity.domain.repository.AppUserRepository;
import com.flirtinghell.profile.domain.model.GuidanceLevel;
import com.flirtinghell.profile.domain.model.UserProfile;
import com.flirtinghell.profile.domain.repository.UserProfileRepository;
import org.springframework.stereotype.Service;

@Service
public class UserBootstrapService {

	private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

	private final AppUserRepository appUserRepository;
	private final UserProfileRepository userProfileRepository;
	private final Clock clock;

	public UserBootstrapService(
			AppUserRepository appUserRepository,
			UserProfileRepository userProfileRepository,
			Clock clock
	) {
		this.appUserRepository = appUserRepository;
		this.userProfileRepository = userProfileRepository;
		this.clock = clock;
	}

	public BootstrapResult bootstrap(String firebaseUid) {
		Instant now = clock.instant();
		AppUser user = appUserRepository.findByFirebaseUid(firebaseUid)
				.map(existing -> touch(existing, now))
				.orElseGet(() -> createUser(firebaseUid, now));
		UserProfile profile = userProfileRepository.findByUserId(user.id())
				.orElseGet(() -> createDefaultProfile(user.id(), now));

		return new BootstrapResult(
				new UserResult(
						user.id(),
						user.onboardingCompleted(),
						new ProfileResult(
								profile.nickname(),
								profile.speechStyle(),
								profile.datingStyle(),
								profile.guidanceLevel(),
								profile.preferredPartnerStyle(),
								profile.avoidAdvice()
						)
				),
				createUsageResult(now),
				List.of()
		);
	}

	public ProfileResult updateProfile(String firebaseUid, UpdateProfileCommand command) {
		BootstrapResult bootstrapResult = bootstrap(firebaseUid);
		Instant now = clock.instant();
		UserProfile current = userProfileRepository.findByUserId(bootstrapResult.user().userId())
				.orElseThrow();
		UserProfile updated = current.update(
				command.nickname(),
				command.speechStyle(),
				command.datingStyle(),
				command.guidanceLevel(),
				command.preferredPartnerStyle(),
				command.avoidAdvice(),
				now
		);
		UserProfile saved = userProfileRepository.save(updated);

		return new ProfileResult(
				saved.nickname(),
				saved.speechStyle(),
				saved.datingStyle(),
				saved.guidanceLevel(),
				saved.preferredPartnerStyle(),
				saved.avoidAdvice()
		);
	}

	private AppUser touch(AppUser user, Instant now) {
		AppUser touched = new AppUser(
				user.id(),
				user.firebaseUid(),
				user.status(),
				user.onboardingCompleted(),
				user.createdAt(),
				now,
				now
		);
		return appUserRepository.save(touched);
	}

	private AppUser createUser(String firebaseUid, Instant now) {
		String userId = "usr_" + Integer.toUnsignedString(firebaseUid.hashCode(), 36);
		AppUser user = new AppUser(
				userId,
				firebaseUid,
				AppUserStatus.ACTIVE,
				false,
				now,
				now,
				now
		);
		return appUserRepository.save(user);
	}

	private UserProfile createDefaultProfile(String userId, Instant now) {
		UserProfile profile = new UserProfile(
				userId,
				null,
				"내 말투를 자연스럽게 유지",
				"천천히 확인하면서 대화 이어가기",
				GuidanceLevel.BALANCED,
				null,
				"단정하거나 압박하는 조언은 피하기",
				now,
				now
		);
		return userProfileRepository.save(profile);
	}

	private UsageResult createUsageResult(Instant now) {
		LocalDate usageDate = LocalDate.ofInstant(now, SEOUL_ZONE);
		return new UsageResult(
				0,
				new DailyLimitResult(usageDate, 3, 0, 3),
				new DailyLimitResult(usageDate, 3, 0, 3)
		);
	}

	public record BootstrapResult(
			UserResult user,
			UsageResult usage,
			List<RecentRoomResult> recentRooms
	) {
	}

	public record UserResult(
			String userId,
			boolean onboardingCompleted,
			ProfileResult profile
	) {
	}

	public record ProfileResult(
			String nickname,
			String speechStyle,
			String datingStyle,
			GuidanceLevel guidanceLevel,
			String preferredPartnerStyle,
			String avoidAdvice
	) {
	}

	public record UsageResult(
			int creditBalance,
			DailyLimitResult freeAnalyses,
			DailyLimitResult rewardAds
	) {
	}

	public record DailyLimitResult(
			LocalDate date,
			int limit,
			int used,
			int remaining
	) {
	}

	public record RecentRoomResult(
			String roomId,
			String alias,
			String relationshipStage,
			String currentConcern,
			String lastTurnSummary,
			Instant lastActivityAt,
			int savedReplyCount
	) {
	}

	public record UpdateProfileCommand(
			String nickname,
			String speechStyle,
			String datingStyle,
			GuidanceLevel guidanceLevel,
			String preferredPartnerStyle,
			String avoidAdvice
	) {
	}
}
