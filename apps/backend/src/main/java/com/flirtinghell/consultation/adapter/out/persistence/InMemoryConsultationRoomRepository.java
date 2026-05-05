package com.flirtinghell.consultation.adapter.out.persistence;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.flirtinghell.consultation.domain.model.ConsultationRoom;
import com.flirtinghell.consultation.domain.repository.ConsultationRoomRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!local")
class InMemoryConsultationRoomRepository implements ConsultationRoomRepository {

	private final ConcurrentMap<String, ConsultationRoom> roomsById = new ConcurrentHashMap<>();

	@Override
	public ConsultationRoom save(ConsultationRoom room) {
		roomsById.put(room.id(), room);
		return room;
	}

	@Override
	public List<ConsultationRoom> findRecentByUserId(String userId, int limit) {
		return roomsById.values().stream()
				.filter(room -> room.userId().equals(userId))
				.filter(room -> room.archivedAt() == null)
				.sorted(Comparator.comparing(ConsultationRoom::updatedAt).reversed())
				.limit(limit)
				.toList();
	}

	@Override
	public Optional<ConsultationRoom> findByIdAndUserId(String roomId, String userId) {
		return Optional.ofNullable(roomsById.get(roomId))
				.filter(room -> room.userId().equals(userId))
				.filter(room -> room.archivedAt() == null);
	}
}
