package com.flirtinghell.consultation.domain.model;

import java.time.Instant;

public record ConsultationRoom(
		String id,
		String userId,
		String alias,
		RelationshipStage relationshipStage,
		String currentConcern,
		String cautionNotes,
		StrategyId preferredStrategyId,
		String lastTurnSummary,
		int savedReplyCount,
		Instant archivedAt,
		Instant createdAt,
		Instant updatedAt
) {
	public static ConsultationRoom create(
			String id,
			String userId,
			String alias,
			RelationshipStage relationshipStage,
			String currentConcern,
			String cautionNotes,
			StrategyId preferredStrategyId,
			Instant now
	) {
		return new ConsultationRoom(
				id,
				userId,
				alias,
				relationshipStage,
				currentConcern,
				cautionNotes,
				preferredStrategyId,
				"아직 분석 기록이 없어요",
				0,
				null,
				now,
				now
		);
	}

	public ConsultationRoom withAnalysisResult(String summary, Instant now) {
		return new ConsultationRoom(
				id,
				userId,
				alias,
				relationshipStage,
				currentConcern,
				cautionNotes,
				preferredStrategyId,
				summary,
				savedReplyCount + 1,
				archivedAt,
				createdAt,
				now
		);
	}
}
