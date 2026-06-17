package com.flirtinghell.analysis.application.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.flirtinghell.analysis.application.port.out.AnalysisPort;
import com.flirtinghell.analysis.domain.model.AnalysisTurn;
import com.flirtinghell.analysis.domain.model.InputSourceType;
import com.flirtinghell.analysis.domain.repository.AnalysisTurnRepository;
import com.flirtinghell.consultation.domain.model.ConsultationRoom;
import com.flirtinghell.consultation.domain.model.StrategyId;
import com.flirtinghell.consultation.domain.repository.ConsultationRoomRepository;
import com.flirtinghell.identity.application.service.UserBootstrapService;
import com.flirtinghell.shared.error.ResourceNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AnalysisService {

	private final UserBootstrapService userBootstrapService;
	private final ConsultationRoomRepository consultationRoomRepository;
	private final AnalysisTurnRepository analysisTurnRepository;
	private final AnalysisPort analysisPort;
	private final Clock clock;

	public AnalysisService(
			UserBootstrapService userBootstrapService,
			ConsultationRoomRepository consultationRoomRepository,
			AnalysisTurnRepository analysisTurnRepository,
			AnalysisPort analysisPort,
			Clock clock
	) {
		this.userBootstrapService = userBootstrapService;
		this.consultationRoomRepository = consultationRoomRepository;
		this.analysisTurnRepository = analysisTurnRepository;
		this.analysisPort = analysisPort;
		this.clock = clock;
	}

	public AnalysisTurnResult createAnalysis(String firebaseUid, String roomId, CreateAnalysisCommand command) {
		UserBootstrapService.UserResult userResult = userBootstrapService.bootstrap(firebaseUid).user();
		String userId = userResult.userId();
		ConsultationRoom room = consultationRoomRepository.findByIdAndUserId(roomId, userId)
				.orElseThrow(() -> new ResourceNotFoundException("ROOM_NOT_FOUND", "상담방을 찾을 수 없습니다."));
		Instant now = clock.instant();
		StrategyId strategyId = resolveStrategy(command.requestedStrategyId(), room.preferredStrategyId());

		// 메모리 Phase A: 이전 turn에서 연속성 맥락(요약·최신 상대유형)을 조립한다.
		List<AnalysisTurn> recentTurns =
				analysisTurnRepository.findRecentByRoomIdAndUserId(roomId, userId, 3);
		List<String> recentSummaries = recentTurns.stream()
				.map(AnalysisTurn::summary)
				.toList();
		String latestPartnerType = recentTurns.stream()
				.map(AnalysisTurn::partnerType)
				.filter(value -> value != null && !value.isBlank())
				.findFirst()
				.orElse(null);

		AnalysisPort.AnalysisDraft draft = analysisPort.analyze(new AnalysisPort.AnalysisRequest(
				room.alias(),
				room.relationshipStage(),
				room.currentConcern(),
				room.cautionNotes(),
				strategyId,
				command.rawInput(),
				userResult.profile().personalitySelf(),
				userResult.profile().personalityIdeal(),
				recentSummaries,
				latestPartnerType
		));
		AnalysisTurn turn = toTurn(userId, room.id(), draft, now);
		AnalysisTurn savedTurn = analysisTurnRepository.save(turn);
		consultationRoomRepository.save(room.withAnalysisResult(savedTurn.summary(), now));
		return AnalysisTurnResult.from(savedTurn);
	}

	public RefineResult refineReply(String firebaseUid, String roomId, RefineCommand command) {
		String userId = userBootstrapService.bootstrap(firebaseUid).user().userId();
		consultationRoomRepository.findByIdAndUserId(roomId, userId)
				.orElseThrow(() -> new ResourceNotFoundException("ROOM_NOT_FOUND", "상담방을 찾을 수 없습니다."));
		String latestPartnerType = analysisTurnRepository
				.findRecentByRoomIdAndUserId(roomId, userId, 1).stream()
				.map(AnalysisTurn::partnerType)
				.filter(value -> value != null && !value.isBlank())
				.findFirst()
				.orElse(null);
		String reply = analysisPort.refineReply(new AnalysisPort.RefineRequest(
				command.previousReply(),
				command.direction(),
				latestPartnerType
		));
		return new RefineResult(reply);
	}

	public CoachReplyResult coach(String firebaseUid, String roomId, CoachCommand command) {
		UserBootstrapService.UserResult userResult = userBootstrapService.bootstrap(firebaseUid).user();
		String userId = userResult.userId();
		ConsultationRoom room = consultationRoomRepository.findByIdAndUserId(roomId, userId)
				.orElseThrow(() -> new ResourceNotFoundException("ROOM_NOT_FOUND", "상담방을 찾을 수 없습니다."));
		String latestPartnerType = analysisTurnRepository
				.findRecentByRoomIdAndUserId(roomId, userId, 1).stream()
				.map(AnalysisTurn::partnerType)
				.filter(value -> value != null && !value.isBlank())
				.findFirst()
				.orElse(null);
		String reply = analysisPort.coachReply(new AnalysisPort.CoachRequest(
				command.history(),
				command.userMessage(),
				room.currentConcern(),
				latestPartnerType,
				userResult.profile().personalityIdeal()
		));
		return new CoachReplyResult(reply);
	}

	private StrategyId resolveStrategy(StrategyId requestedStrategyId, StrategyId preferredStrategyId) {
		if (requestedStrategyId != null) {
			return requestedStrategyId;
		}
		if (preferredStrategyId != null) {
			return preferredStrategyId;
		}
		return StrategyId.DEVELOP_ROMANCE;
	}

	private AnalysisTurn toTurn(
			String userId,
			String roomId,
			AnalysisPort.AnalysisDraft draft,
			Instant now
	) {
		return new AnalysisTurn(
				"turn_" + UUID.randomUUID().toString().replace("-", ""),
				roomId,
				userId,
				draft.sourceType(),
				draft.participantSummary(),
				draft.summary(),
				draft.currentState(),
				draft.recommendedStrategyId(),
				draft.warnings(),
				draft.primaryReply(),
				draft.alternativeReplies(),
				draft.replyReason(),
				draft.nextAction(),
				draft.partnerType(),
				now
		);
	}

	public record CreateAnalysisCommand(
			String rawInput,
			StrategyId requestedStrategyId
	) {
	}

	public record RefineCommand(
			String previousReply,
			AnalysisPort.RefineDirection direction
	) {
	}

	public record RefineResult(String reply) {
	}

	public record CoachCommand(
			List<AnalysisPort.CoachMessage> history,
			String userMessage
	) {
	}

	public record CoachReplyResult(String reply) {
	}

	public record AnalysisTurnResult(
			String turnId,
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
			String partnerType,
			Instant createdAt
	) {
		public static AnalysisTurnResult from(AnalysisTurn turn) {
			return new AnalysisTurnResult(
					turn.id(),
					turn.sourceType(),
					turn.participantSummary(),
					turn.summary(),
					turn.currentState(),
					turn.recommendedStrategyId(),
					turn.warnings(),
					turn.primaryReply(),
					turn.alternativeReplies(),
					turn.replyReason(),
					turn.nextAction(),
					turn.partnerType(),
					turn.createdAt()
			);
		}
	}
}
