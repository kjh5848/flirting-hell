package com.flirtinghell.consultation.domain.repository;

import java.util.List;
import java.util.Optional;

import com.flirtinghell.consultation.domain.model.ConsultationRoom;

public interface ConsultationRoomRepository {

	ConsultationRoom save(ConsultationRoom room);

	List<ConsultationRoom> findRecentByUserId(String userId, int limit);

	Optional<ConsultationRoom> findByIdAndUserId(String roomId, String userId);
}
