package com.flirtinghell.analysis.adapter.out.persistence;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import com.flirtinghell.analysis.domain.model.AnalysisTurn;
import com.flirtinghell.analysis.domain.model.InputSourceType;
import com.flirtinghell.consultation.domain.model.StrategyId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "analysis_turns")
class JpaAnalysisTurnEntity {

	private static final String LIST_SEPARATOR = "\n";

	@Id
	private String id;

	@Column(name = "room_id", nullable = false)
	private String roomId;

	@Column(name = "user_id", nullable = false)
	private String userId;

	@Enumerated(EnumType.STRING)
	@Column(name = "source_type", nullable = false)
	private InputSourceType sourceType;

	@Column(name = "participant_summary", nullable = false)
	private String participantSummary;

	@Column(nullable = false)
	private String summary;

	@Column(name = "current_state", nullable = false)
	private String currentState;

	@Enumerated(EnumType.STRING)
	@Column(name = "recommended_strategy_id", nullable = false)
	private StrategyId recommendedStrategyId;

	@Column(nullable = false)
	private String warnings;

	@Column(name = "primary_reply", nullable = false)
	private String primaryReply;

	@Column(name = "alternative_replies", nullable = false)
	private String alternativeReplies;

	@Column(name = "reply_reason", nullable = false)
	private String replyReason;

	@Column(name = "next_action", nullable = false)
	private String nextAction;

	@Column(name = "partner_type")
	private String partnerType;

	@Column(name = "saved", nullable = false)
	private boolean saved;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected JpaAnalysisTurnEntity() {
	}

	private JpaAnalysisTurnEntity(
			String id,
			String roomId,
			String userId,
			InputSourceType sourceType,
			String participantSummary,
			String summary,
			String currentState,
			StrategyId recommendedStrategyId,
			String warnings,
			String primaryReply,
			String alternativeReplies,
			String replyReason,
			String nextAction,
			String partnerType,
			boolean saved,
			Instant createdAt
	) {
		this.id = id;
		this.roomId = roomId;
		this.userId = userId;
		this.sourceType = sourceType;
		this.participantSummary = participantSummary;
		this.summary = summary;
		this.currentState = currentState;
		this.recommendedStrategyId = recommendedStrategyId;
		this.warnings = warnings;
		this.primaryReply = primaryReply;
		this.alternativeReplies = alternativeReplies;
		this.replyReason = replyReason;
		this.nextAction = nextAction;
		this.partnerType = partnerType;
		this.saved = saved;
		this.createdAt = createdAt;
	}

	static JpaAnalysisTurnEntity fromDomain(AnalysisTurn turn) {
		return new JpaAnalysisTurnEntity(
				turn.id(),
				turn.roomId(),
				turn.userId(),
				turn.sourceType(),
				turn.participantSummary(),
				turn.summary(),
				turn.currentState(),
				turn.recommendedStrategyId(),
				join(turn.warnings()),
				turn.primaryReply(),
				join(turn.alternativeReplies()),
				turn.replyReason(),
				turn.nextAction(),
				turn.partnerType(),
				turn.saved(),
				turn.createdAt()
		);
	}

	AnalysisTurn toDomain() {
		return new AnalysisTurn(
				id,
				roomId,
				userId,
				sourceType,
				participantSummary,
				summary,
				currentState,
				recommendedStrategyId,
				split(warnings),
				primaryReply,
				split(alternativeReplies),
				replyReason,
				nextAction,
				partnerType,
				saved,
				createdAt
		);
	}

	private static String join(List<String> values) {
		return String.join(LIST_SEPARATOR, values);
	}

	private static List<String> split(String value) {
		if (value == null || value.isBlank()) {
			return List.of();
		}
		return Arrays.stream(value.split(LIST_SEPARATOR))
				.toList();
	}
}
