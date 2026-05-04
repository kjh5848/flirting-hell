package com.flirtinghell.identity.adapter.out.persistence;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.flirtinghell.identity.domain.model.AppUser;
import com.flirtinghell.identity.domain.repository.AppUserRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!local")
class InMemoryAppUserRepository implements AppUserRepository {

	private final ConcurrentMap<String, AppUser> usersByFirebaseUid = new ConcurrentHashMap<>();

	@Override
	public Optional<AppUser> findByFirebaseUid(String firebaseUid) {
		return Optional.ofNullable(usersByFirebaseUid.get(firebaseUid));
	}

	@Override
	public AppUser save(AppUser user) {
		usersByFirebaseUid.put(user.firebaseUid(), user);
		return user;
	}
}
