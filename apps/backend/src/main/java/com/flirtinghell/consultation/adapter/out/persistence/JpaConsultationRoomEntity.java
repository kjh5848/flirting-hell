package com.flirtinghell.consultation.adapter.out.persistence;

import java.time.Instant;

import com.flirtinghell.consultation.domain.model.ConsultationRoom;
import com.flirtinghell.consultation.domain.model.RelationshipStage;
import com.flirtinghell.consultation.domain.model.StrategyId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "consultation_rooms")
class JpaConsultationRoomEntity {

	@Id
	private String id;

	@Column(name = "user_id", nullable = false)
	private String userId;

	@Column(nullable = false)
	private String alias;

	@Enumerated(EnumType.STRING)
	@Column(name = "relationship_stage", nullable = false)
	private RelationshipStage relationshipStage;

	@Column(name = "current_concern")
	private String currentConcern;

	@Column(name = "caution_notes")
	private String cautionNotes;

	@Enumerated(EnumType.STRING)
	@Column(name = "preferred_strategy_id")
	private StrategyId preferredStrategyId;

	@Column(name = "archived_at")
	private Instant archivedAt;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected JpaConsultationRoomEntity() {
	}

	private JpaConsultationRoomEntity(
			String id,
			String userId,
			String alias,
			RelationshipStage relationshipStage,
			String currentConcern,
			String cautionNotes,
			StrategyId preferredStrategyId,
			Instant archivedAt,
			Instant createdAt,
			Instant updatedAt
	) {
		this.id = id;
		this.userId = userId;
		this.alias = alias;
		this.relationshipStage = relationshipStage;
		this.currentConcern = currentConcern;
		this.cautionNotes = cautionNotes;
		this.preferredStrategyId = preferredStrategyId;
		this.archivedAt = archivedAt;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	static JpaConsultationRoomEntity fromDomain(ConsultationRoom room) {
		return new JpaConsultationRoomEntity(
				room.id(),
				room.userId(),
				room.alias(),
				room.relationshipStage(),
				room.currentConcern(),
				room.cautionNotes(),
				room.preferredStrategyId(),
				room.archivedAt(),
				room.createdAt(),
				room.updatedAt()
		);
	}

	ConsultationRoom toDomain() {
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
				archivedAt,
				createdAt,
				updatedAt
		);
	}
}
