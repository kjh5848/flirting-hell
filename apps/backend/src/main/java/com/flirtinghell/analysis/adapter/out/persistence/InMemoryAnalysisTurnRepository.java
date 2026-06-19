package com.flirtinghell.analysis.adapter.out.persistence;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.flirtinghell.analysis.domain.model.AnalysisTurn;
import com.flirtinghell.analysis.domain.repository.AnalysisTurnRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!local")
class InMemoryAnalysisTurnRepository implements AnalysisTurnRepository {

	private final ConcurrentMap<String, AnalysisTurn> turnsById = new ConcurrentHashMap<>();

	@Override
	public AnalysisTurn save(AnalysisTurn turn) {
		turnsById.put(turn.id(), turn);
		return turn;
	}

	@Override
	public List<AnalysisTurn> findRecentByRoomIdAndUserId(String roomId, String userId, int limit) {
		return turnsById.values().stream()
				.filter(turn -> turn.roomId().equals(roomId))
				.filter(turn -> turn.userId().equals(userId))
				.sorted(Comparator.comparing(AnalysisTurn::createdAt).reversed())
				.limit(limit)
				.toList();
	}

	@Override
	public Optional<AnalysisTurn> findByIdAndUserId(String turnId, String userId) {
		return turnsById.values().stream()
				.filter(turn -> turn.id().equals(turnId))
				.filter(turn -> turn.userId().equals(userId))
				.findFirst();
	}

	@Override
	public List<AnalysisTurn> findSavedByUserId(String userId, int limit) {
		return turnsById.values().stream()
				.filter(turn -> turn.userId().equals(userId))
				.filter(AnalysisTurn::saved)
				.sorted(Comparator.comparing(AnalysisTurn::createdAt).reversed())
				.limit(limit)
				.toList();
	}
}
