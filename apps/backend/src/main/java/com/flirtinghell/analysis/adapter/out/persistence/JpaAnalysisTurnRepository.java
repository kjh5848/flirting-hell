package com.flirtinghell.analysis.adapter.out.persistence;

import java.util.List;

import com.flirtinghell.analysis.domain.model.AnalysisTurn;
import com.flirtinghell.analysis.domain.repository.AnalysisTurnRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("local")
class JpaAnalysisTurnRepository implements AnalysisTurnRepository {

	private final SpringDataAnalysisTurnRepository repository;

	JpaAnalysisTurnRepository(SpringDataAnalysisTurnRepository repository) {
		this.repository = repository;
	}

	@Override
	public AnalysisTurn save(AnalysisTurn turn) {
		return repository.saveAndFlush(JpaAnalysisTurnEntity.fromDomain(turn)).toDomain();
	}

	@Override
	public List<AnalysisTurn> findRecentByRoomIdAndUserId(String roomId, String userId, int limit) {
		return repository.findTop20ByRoomIdAndUserIdOrderByCreatedAtDesc(roomId, userId)
				.stream()
				.limit(limit)
				.map(JpaAnalysisTurnEntity::toDomain)
				.toList();
	}
}
