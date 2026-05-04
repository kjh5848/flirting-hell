package com.flirtinghell.profile.adapter;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataUserProfileRepository extends JpaRepository<JpaUserProfileEntity, String> {
}
