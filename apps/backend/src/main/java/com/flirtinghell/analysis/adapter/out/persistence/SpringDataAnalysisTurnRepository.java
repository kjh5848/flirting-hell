package com.flirtinghell.analysis.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataAnalysisTurnRepository extends JpaRepository<JpaAnalysisTurnEntity, String> {

	List<JpaAnalysisTurnEntity> findTop20ByRoomIdAndUserIdOrderByCreatedAtDesc(String roomId, String userId);

	Optional<JpaAnalysisTurnEntity> findByIdAndUserId(String id, String userId);

	List<JpaAnalysisTurnEntity> findTop50ByUserIdAndSavedIsTrueOrderByCreatedAtDesc(String userId);
}
