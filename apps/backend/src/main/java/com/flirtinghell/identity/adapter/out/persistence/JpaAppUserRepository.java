package com.flirtinghell.identity.adapter.out.persistence;

import java.util.Optional;

import com.flirtinghell.identity.domain.model.AppUser;
import com.flirtinghell.identity.domain.repository.AppUserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("local")
class JpaAppUserRepository implements AppUserRepository {

	private final SpringDataAppUserRepository repository;

	JpaAppUserRepository(SpringDataAppUserRepository repository) {
		this.repository = repository;
	}

	@Override
	public Optional<AppUser> findByFirebaseUid(String firebaseUid) {
		return repository.findByFirebaseUid(firebaseUid)
				.map(JpaAppUserEntity::toDomain);
	}

	@Override
	public AppUser save(AppUser user) {
		try {
			return repository.saveAndFlush(JpaAppUserEntity.fromDomain(user)).toDomain();
		} catch (DataIntegrityViolationException exception) {
			return repository.findByFirebaseUid(user.firebaseUid())
					.orElseThrow(() -> exception)
					.toDomain();
		}
	}
}
