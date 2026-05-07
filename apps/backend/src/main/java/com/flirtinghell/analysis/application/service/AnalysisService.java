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
		String userId = userBootstrapService.bootstrap(firebaseUid).user().userId();
		ConsultationRoom room = consultationRoomRepository.findByIdAndUserId(roomId, userId)
				.orElseThrow(() -> new ResourceNotFoundException("ROOM_NOT_FOUND", "상담방을 찾을 수 없습니다."));
		Instant now = clock.instant();
		StrategyId strategyId = resolveStrategy(command.requestedStrategyId(), room.preferredStrategyId());
		AnalysisPort.AnalysisDraft draft = analysisPort.analyze(new AnalysisPort.AnalysisRequest(
				room.alias(),
				room.relationshipStage(),
				room.currentConcern(),
				room.cautionNotes(),
				strategyId,
				command.rawInput()
		));
		AnalysisTurn turn = toTurn(userId, room.id(), draft, now);
		AnalysisTurn savedTurn = analysisTurnRepository.save(turn);
		consultationRoomRepository.save(room.withAnalysisResult(savedTurn.summary(), now));
		return AnalysisTurnResult.from(savedTurn);
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
				now
		);
	}

	public record CreateAnalysisCommand(
			String rawInput,
			StrategyId requestedStrategyId
	) {
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
					turn.createdAt()
			);
		}
	}
}
