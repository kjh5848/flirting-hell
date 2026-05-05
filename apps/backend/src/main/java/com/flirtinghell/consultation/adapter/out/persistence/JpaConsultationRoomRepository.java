package com.flirtinghell.consultation.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import com.flirtinghell.consultation.domain.model.ConsultationRoom;
import com.flirtinghell.consultation.domain.repository.ConsultationRoomRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("local")
class JpaConsultationRoomRepository implements ConsultationRoomRepository {

	private final SpringDataConsultationRoomRepository repository;

	JpaConsultationRoomRepository(SpringDataConsultationRoomRepository repository) {
		this.repository = repository;
	}

	@Override
	public ConsultationRoom save(ConsultationRoom room) {
		return repository.saveAndFlush(JpaConsultationRoomEntity.fromDomain(room)).toDomain();
	}

	@Override
	public List<ConsultationRoom> findRecentByUserId(String userId, int limit) {
		return repository.findTop20ByUserIdAndArchivedAtIsNullOrderByUpdatedAtDesc(userId)
				.stream()
				.limit(limit)
				.map(JpaConsultationRoomEntity::toDomain)
				.toList();
	}

	@Override
	public Optional<ConsultationRoom> findByIdAndUserId(String roomId, String userId) {
		return repository.findByIdAndUserIdAndArchivedAtIsNull(roomId, userId)
				.map(JpaConsultationRoomEntity::toDomain);
	}
}
