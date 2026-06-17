package com.flirtinghell.analysis.application.port.out;

import java.util.List;

import com.flirtinghell.analysis.domain.model.InputSourceType;
import com.flirtinghell.consultation.domain.model.RelationshipStage;
import com.flirtinghell.consultation.domain.model.StrategyId;

public interface AnalysisPort {

	AnalysisDraft analyze(AnalysisRequest request);

	/// 답장을 원하는 톤 방향으로 다시 제안한다(비영속 ephemeral). 새 turn을 만들지 않는다.
	String refineReply(RefineRequest request);

	/// 데이트 플랜을 제안한다(비영속). 코스뿐 아니라 상대 식습관·취향을 자연스럽게
	/// 확인해 "궁합"을 같이 가늠하는 확인 포인트를 포함한다.
	PlanDraft suggestPlan(PlanRequest request);

	record PlanRequest(
			RelationshipStage relationshipStage,
			String currentConcern,
			String latestPartnerType,
			String myPersonalityIdeal
	) {
	}

	record PlanStep(String title, String detail) {
	}

	record PlanDraft(
			String theme,
			List<PlanStep> steps,
			List<String> checkPoints,
			List<String> cautions
	) {
		public PlanDraft {
			steps = List.copyOf(steps);
			checkPoints = List.copyOf(checkPoints);
			cautions = List.copyOf(cautions);
		}
	}

	/// 코치와의 멀티턴 대화에 한 번 응답한다(상태 비저장). 히스토리는 클라이언트가
	/// 매 호출에 보낸다 — 서버는 대화 원문을 저장하지 않는다(메모리 설계 원칙).
	String coachReply(CoachRequest request);

	enum CoachRole {
		USER,
		COACH
	}

	record CoachMessage(CoachRole role, String text) {
	}

	record CoachRequest(
			List<CoachMessage> history,
			String userMessage,
			String roomConcern,
			String latestPartnerType,
			String myPersonalityIdeal
	) {
		public CoachRequest {
			history = history == null ? List.of() : List.copyOf(history);
		}
	}

	enum RefineDirection {
		LIGHTER,
		MORE_SERIOUS,
		SLOWER,
		BOLDER
	}

	record RefineRequest(
			String previousReply,
			RefineDirection direction,
			String latestPartnerType
	) {
	}

	record AnalysisRequest(
			String roomAlias,
			RelationshipStage relationshipStage,
			String currentConcern,
			String cautionNotes,
			StrategyId requestedStrategyId,
			String rawInput,
			// 대화 연속성·개인화용 맥락(메모리 Phase A). 전부 nullable.
			String myPersonalitySelf,
			String myPersonalityIdeal,
			List<String> recentSummaries,
			String latestPartnerType
	) {
		public AnalysisRequest {
			recentSummaries = recentSummaries == null ? List.of() : List.copyOf(recentSummaries);
		}
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
