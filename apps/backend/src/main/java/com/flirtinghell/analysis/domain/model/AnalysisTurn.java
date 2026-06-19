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
		// 상대 5축 성향 추론(JSON 문자열, nullable). 과거 분석에는 없을 수 있다.
		String partnerType,
		// 사용자가 저장(북마크)한 답장인지.
		boolean saved,
		Instant createdAt
) {
	public AnalysisTurn {
		warnings = List.copyOf(warnings);
		alternativeReplies = List.copyOf(alternativeReplies);
	}

	public AnalysisTurn markSaved(boolean value) {
		return new AnalysisTurn(
				id, roomId, userId, sourceType, participantSummary, summary,
				currentState, recommendedStrategyId, warnings, primaryReply,
				alternativeReplies, replyReason, nextAction, partnerType, value,
				createdAt
		);
	}
}
