package com.flirtinghell.analysis.domain.repository;

import java.util.List;
import java.util.Optional;

import com.flirtinghell.analysis.domain.model.AnalysisTurn;

public interface AnalysisTurnRepository {

	AnalysisTurn save(AnalysisTurn turn);

	List<AnalysisTurn> findRecentByRoomIdAndUserId(String roomId, String userId, int limit);

	Optional<AnalysisTurn> findByIdAndUserId(String turnId, String userId);

	List<AnalysisTurn> findSavedByUserId(String userId, int limit);
}
