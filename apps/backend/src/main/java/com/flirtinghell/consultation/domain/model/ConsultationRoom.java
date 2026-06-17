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
		// 메모리 Phase A.5: 분석이 쌓여도 일정 크기로 유지되는 구조화 관계상태(JSON, nullable).
		String relationshipState,
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
				null,
				0,
				null,
				now,
				now
		);
	}

	public ConsultationRoom withAnalysisResult(String summary, String relationshipState, Instant now) {
		return new ConsultationRoom(
				id,
				userId,
				alias,
				relationshipStage,
				currentConcern,
				cautionNotes,
				preferredStrategyId,
				summary,
				relationshipState,
				savedReplyCount + 1,
				archivedAt,
				createdAt,
				now
		);
	}
}
