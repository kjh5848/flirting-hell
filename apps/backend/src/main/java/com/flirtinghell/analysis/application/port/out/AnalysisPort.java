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
			String nextAction,
			// 상대 5축 성향 추론 결과(JSON 문자열, nullable). 백엔드는 의미를
			// 해석하지 않고 클라이언트가 5축으로 파싱한다.
			String partnerType
	) {
		public AnalysisDraft {
			warnings = List.copyOf(warnings);
			alternativeReplies = List.copyOf(alternativeReplies);
		}
	}
}
