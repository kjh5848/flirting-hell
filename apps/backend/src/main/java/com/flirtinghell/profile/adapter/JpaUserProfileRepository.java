package com.flirtinghell.profile.adapter;

import java.util.Optional;

import com.flirtinghell.profile.domain.model.UserProfile;
import com.flirtinghell.profile.domain.repository.UserProfileRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("local")
class JpaUserProfileRepository implements UserProfileRepository {

	private final SpringDataUserProfileRepository repository;

	JpaUserProfileRepository(SpringDataUserProfileRepository repository) {
		this.repository = repository;
	}

	@Override
	public Optional<UserProfile> findByUserId(String userId) {
		return repository.findById(userId)
				.map(JpaUserProfileEntity::toDomain);
	}

	@Override
	public UserProfile save(UserProfile profile) {
		try {
			return repository.saveAndFlush(JpaUserProfileEntity.fromDomain(profile)).toDomain();
		} catch (DataIntegrityViolationException exception) {
			return repository.findById(profile.userId())
					.orElseThrow(() -> exception)
					.toDomain();
		}
	}
}
