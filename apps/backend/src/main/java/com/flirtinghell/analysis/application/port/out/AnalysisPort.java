package com.flirtinghell.analysis.application.port.out;

import java.util.List;

import com.flirtinghell.analysis.domain.model.InputSourceType;
import com.flirtinghell.consultation.domain.model.RelationshipStage;
import com.flirtinghell.consultation.domain.model.StrategyId;

public interface AnalysisPort {

	AnalysisDraft analyze(AnalysisRequest request);

	record AnalysisRequest(
			String roomAlias,
			RelationshipStage relationshipStage,
			String currentConcern,
			String cautionNotes,
			StrategyId requestedStrategyId,
			String rawInput
	) {
	}

	record AnalysisDraft(
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
		public AnalysisDraft {
			warnings = List.copyOf(warnings);
			alternativeReplies = List.copyOf(alternativeReplies);
		}
	}
}
