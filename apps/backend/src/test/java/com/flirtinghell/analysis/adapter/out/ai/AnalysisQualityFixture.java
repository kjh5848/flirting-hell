package com.flirtinghell.analysis.adapter.out.ai;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flirtinghell.analysis.application.port.out.AnalysisPort;
import com.flirtinghell.analysis.domain.model.InputSourceType;
import com.flirtinghell.consultation.domain.model.RelationshipStage;
import com.flirtinghell.consultation.domain.model.StrategyId;

record AnalysisQualityFixture(
		String id,
		String title,
		String roomAlias,
		RelationshipStage relationshipStage,
		String currentConcern,
		String cautionNotes,
		StrategyId requestedStrategyId,
		String rawInput,
		ExpectedDraft expectedDraft,
		List<String> forbiddenReplyTerms
) {
	AnalysisPort.AnalysisRequest toRequest() {
		return new AnalysisPort.AnalysisRequest(
				roomAlias,
				relationshipStage,
				currentConcern,
				cautionNotes,
				requestedStrategyId,
				rawInput,
				null,
				null,
				List.of(),
				null
		);
	}

	InputSourceType expectedSourceType() {
		return expectedDraft.sourceType();
	}

	StrategyId expectedRecommendedStrategyId() {
		return expectedDraft.recommendedStrategyId();
	}

	String expectedResponseJson(ObjectMapper objectMapper) {
		try {
			return objectMapper.writeValueAsString(expectedDraft);
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("Fixture response JSON serialization failed: " + id, exception);
		}
	}

	@Override
	public String toString() {
		return id + " - " + title;
	}

	record ExpectedDraft(
			InputSourceType sourceType,
			String participantSummary,
			String summary,
			String currentState,
			StrategyId recommendedStrategyId,
			List<String> warnings,
			String primaryReply,
			List<String> alternativeReplies,
			String replyReason,
			String nextAction
	) {
	}
}
