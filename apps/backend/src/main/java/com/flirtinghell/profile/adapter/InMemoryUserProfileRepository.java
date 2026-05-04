package com.flirtinghell.profile.adapter;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.flirtinghell.profile.domain.model.UserProfile;
import com.flirtinghell.profile.domain.repository.UserProfileRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!local")
class InMemoryUserProfileRepository implements UserProfileRepository {

	private final ConcurrentMap<String, UserProfile> profilesByUserId = new ConcurrentHashMap<>();

	@Override
	public Optional<UserProfile> findByUserId(String userId) {
		return Optional.ofNullable(profilesByUserId.get(userId));
	}

	@Override
	public UserProfile save(UserProfile profile) {
		profilesByUserId.put(profile.userId(), profile);
		return profile;
	}
}
