package com.flirtinghell.analysis.domain.repository;

import java.util.List;

import com.flirtinghell.analysis.domain.model.AnalysisTurn;

public interface AnalysisTurnRepository {

	AnalysisTurn save(AnalysisTurn turn);

	List<AnalysisTurn> findRecentByRoomIdAndUserId(String roomId, String userId, int limit);
}
