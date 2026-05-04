package com.flirtinghell.identity.domain.repository;

import java.util.Optional;

import com.flirtinghell.identity.domain.model.AppUser;

public interface AppUserRepository {

	Optional<AppUser> findByFirebaseUid(String firebaseUid);

	AppUser save(AppUser user);
}
