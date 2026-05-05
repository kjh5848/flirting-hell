package com.flirtinghell.analysis.domain.model;

import java.time.Instant;
import java.util.List;

import com.flirtinghell.consultation.domain.model.StrategyId;

public record AnalysisTurn(
		String id,
		String roomId,
		String userId,
		InputSourceType sourceType,
		String participantSummary,
		String summary,
		String currentState,
		StrategyId recommendedStrategyId,
		List<String> warnings,
		String primaryReply,
		List<String> alternativeReplies,
		String replyReason,
		String nextAction,
		Instant createdAt
) {
	public AnalysisTurn {
		warnings = List.copyOf(warnings);
		alternativeReplies = List.copyOf(alternativeReplies);
	}
}
