package com.flirtinghell.identity.adapter.out.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataAppUserRepository extends JpaRepository<JpaAppUserEntity, String> {

	Optional<JpaAppUserEntity> findByFirebaseUid(String firebaseUid);
}
