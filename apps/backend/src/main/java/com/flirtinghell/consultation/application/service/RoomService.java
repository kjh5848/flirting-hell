package com.flirtinghell.consultation.application.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.flirtinghell.analysis.application.service.AnalysisService;
import com.flirtinghell.analysis.domain.repository.AnalysisTurnRepository;
import com.flirtinghell.consultation.domain.model.ConsultationRoom;
import com.flirtinghell.consultation.domain.model.RelationshipStage;
import com.flirtinghell.consultation.domain.model.StrategyId;
import com.flirtinghell.consultation.domain.repository.ConsultationRoomRepository;
import com.flirtinghell.identity.application.service.UserBootstrapService;
import com.flirtinghell.shared.error.ResourceNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class RoomService {

	private static final int DEFAULT_ROOM_LIMIT = 20;

	private final UserBootstrapService userBootstrapService;
	private final ConsultationRoomRepository consultationRoomRepository;
	private final AnalysisTurnRepository analysisTurnRepository;
	private final Clock clock;

	public RoomService(
			UserBootstrapService userBootstrapService,
			ConsultationRoomRepository consultationRoomRepository,
			AnalysisTurnRepository analysisTurnRepository,
			Clock clock
	) {
		this.userBootstrapService = userBootstrapService;
		this.consultationRoomRepository = consultationRoomRepository;
		this.analysisTurnRepository = analysisTurnRepository;
		this.clock = clock;
	}

	public RoomResult createRoom(String firebaseUid, CreateRoomCommand command) {
		String userId = userBootstrapService.bootstrap(firebaseUid).user().userId();
		Instant now = clock.instant();
		ConsultationRoom room = ConsultationRoom.create(
				"room_" + UUID.randomUUID().toString().replace("-", ""),
				userId,
				command.alias(),
				command.relationshipStage(),
				command.currentConcern(),
				command.cautionNotes(),
				command.preferredStrategyId(),
				now
		);
		return RoomResult.from(consultationRoomRepository.save(room));
	}

	public RoomListResult listRooms(String firebaseUid) {
		String userId = userBootstrapService.bootstrap(firebaseUid).user().userId();
		List<RoomSummaryResult> rooms = consultationRoomRepository.findRecentByUserId(userId, DEFAULT_ROOM_LIMIT)
				.stream()
				.map(RoomSummaryResult::from)
				.toList();
		return new RoomListResult(rooms, null);
	}

	public RoomDetailResult getRoom(String firebaseUid, String roomId) {
		String userId = userBootstrapService.bootstrap(firebaseUid).user().userId();
		ConsultationRoom room = consultationRoomRepository.findByIdAndUserId(roomId, userId)
				.orElseThrow(() -> new ResourceNotFoundException("ROOM_NOT_FOUND", "상담방을 찾을 수 없습니다."));
		List<AnalysisService.AnalysisTurnResult> recentTurns = analysisTurnRepository
				.findRecentByRoomIdAndUserId(room.id(), userId, 20)
				.stream()
				.map(AnalysisService.AnalysisTurnResult::from)
				.toList();
		return new RoomDetailResult(RoomResult.from(room), recentTurns, List.of());
	}

	public record CreateRoomCommand(
			String alias,
			RelationshipStage relationshipStage,
			String currentConcern,
			String cautionNotes,
			StrategyId preferredStrategyId
	) {
	}

	public record RoomListResult(
			List<RoomSummaryResult> rooms,
			String nextCursor
	) {
	}

	public record RoomSummaryResult(
			String roomId,
			String alias,
			RelationshipStage relationshipStage,
			String currentConcern,
			String lastTurnSummary,
			Instant lastActivityAt,
			int savedReplyCount
	) {
		static RoomSummaryResult from(ConsultationRoom room) {
			return new RoomSummaryResult(
					room.id(),
					room.alias(),
					room.relationshipStage(),
					room.currentConcern(),
					room.lastTurnSummary(),
					room.updatedAt(),
					room.savedReplyCount()
			);
		}
	}

	public record RoomResult(
			String roomId,
			String alias,
			RelationshipStage relationshipStage,
			String currentConcern,
			String cautionNotes,
			StrategyId preferredStrategyId,
			Instant createdAt,
			Instant updatedAt
	) {
		static RoomResult from(ConsultationRoom room) {
			return new RoomResult(
					room.id(),
					room.alias(),
					room.relationshipStage(),
					room.currentConcern(),
					room.cautionNotes(),
					room.preferredStrategyId(),
					room.createdAt(),
					room.updatedAt()
			);
		}
	}

	public record RoomDetailResult(
			RoomResult room,
			List<AnalysisService.AnalysisTurnResult> recentTurns,
			List<Object> savedReplies
	) {
	}
}
