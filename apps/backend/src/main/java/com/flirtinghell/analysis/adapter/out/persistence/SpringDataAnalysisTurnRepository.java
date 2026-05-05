package com.flirtinghell.analysis.adapter.out.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataAnalysisTurnRepository extends JpaRepository<JpaAnalysisTurnEntity, String> {

	List<JpaAnalysisTurnEntity> findTop20ByRoomIdAndUserIdOrderByCreatedAtDesc(String roomId, String userId);
}
