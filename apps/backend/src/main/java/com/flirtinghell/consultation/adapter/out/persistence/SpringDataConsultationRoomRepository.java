package com.flirtinghell.consultation.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataConsultationRoomRepository extends JpaRepository<JpaConsultationRoomEntity, String> {

	List<JpaConsultationRoomEntity> findTop20ByUserIdAndArchivedAtIsNullOrderByUpdatedAtDesc(String userId);

	Optional<JpaConsultationRoomEntity> findByIdAndUserIdAndArchivedAtIsNull(String id, String userId);
}
