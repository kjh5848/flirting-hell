package com.flirtinghell.profile.domain.repository;

import java.util.Optional;

import com.flirtinghell.profile.domain.model.UserProfile;

public interface UserProfileRepository {

	Optional<UserProfile> findByUserId(String userId);

	UserProfile save(UserProfile profile);
}
