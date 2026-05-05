package com.flirtinghell.analysis.application.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

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
	private final Clock clock;

	public AnalysisService(
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

	public AnalysisTurnResult createAnalysis(String firebaseUid, String roomId, CreateAnalysisCommand command) {
		String userId = userBootstrapService.bootstrap(firebaseUid).user().userId();
		ConsultationRoom room = consultationRoomRepository.findByIdAndUserId(roomId, userId)
				.orElseThrow(() -> new ResourceNotFoundException("ROOM_NOT_FOUND", "상담방을 찾을 수 없습니다."));
		Instant now = clock.instant();
		StrategyId strategyId = resolveStrategy(command.requestedStrategyId(), room.preferredStrategyId());
		AnalysisTurn turn = mockTurn(userId, room, command.rawInput(), strategyId, now);
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

	private AnalysisTurn mockTurn(
			String userId,
			ConsultationRoom room,
			String rawInput,
			StrategyId strategyId,
			Instant now
	) {
		InputSourceType sourceType = detectSourceType(rawInput);
		return new AnalysisTurn(
				"turn_" + UUID.randomUUID().toString().replace("-", ""),
				room.id(),
				userId,
				sourceType,
				participantSummary(rawInput),
				summary(rawInput),
				"상대가 편하게 반응하는 중이라 부담 없이 한 번 더 이어갈 수 있어요.",
				strategyId,
				List.of(
						"지금 바로 마음을 확인하려고 몰아붙이지 마세요.",
						"답장이 늦어도 추궁처럼 보이는 표현은 피하세요."
				),
				primaryReply(strategyId, rawInput),
				alternativeReplies(strategyId),
				"상대가 편한 상태를 말했으니, 확인 질문보다 일상 질문으로 대화를 자연스럽게 이어가는 편이 안전합니다.",
				"상대가 답하면 음식, 산책, 영화처럼 가벼운 선택지로 약속 가능성을 봅니다.",
				now
		);
	}

	private InputSourceType detectSourceType(String rawInput) {
		String normalized = rawInput.toLowerCase(Locale.ROOT);
		if (rawInput.contains("나:") || rawInput.contains("상대:") || rawInput.contains("오전") || rawInput.contains("오후")) {
			return InputSourceType.KAKAO;
		}
		if (normalized.contains("dm") || normalized.contains("인스타")) {
			return InputSourceType.DM;
		}
		if (normalized.contains("telegram") || normalized.contains("텔레그램")) {
			return InputSourceType.TELEGRAM;
		}
		if (normalized.contains("문자") || normalized.contains("sms")) {
			return InputSourceType.SMS;
		}
		if (normalized.contains("상황") || !rawInput.contains(":")) {
			return InputSourceType.SITUATION;
		}
		return InputSourceType.UNKNOWN;
	}

	private String participantSummary(String rawInput) {
		if (rawInput.contains("나:") && rawInput.contains("상대:")) {
			return "나와 상대 발화가 구분되어 있어요.";
		}
		return "발화자 구분은 일부 애매해서 요약 중심으로 봅니다.";
	}

	private String summary(String rawInput) {
		if (rawInput.contains("집") || rawInput.contains("쉬")) {
			return "상대가 집에서 쉬는 흐름이라 가볍게 대화를 이어갈 수 있어요.";
		}
		return "상대 반응을 더 확인하면서 가볍게 대화를 이어갈 수 있어요.";
	}

	private String primaryReply(StrategyId strategyId, String rawInput) {
		if (strategyId == StrategyId.MAKE_PLAN && (rawInput.contains("집") || rawInput.contains("쉬"))) {
			return "오 좋다 ㅋㅋ 그럼 오늘은 집에서 충전하는 날이네. 저녁은 뭐 먹을 생각이야?";
		}
		if (strategyId == StrategyId.CHECK_RELATIONSHIP_STATUS) {
			return "그랬구나 ㅋㅋ 요즘은 누구랑 제일 자주 놀아?";
		}
		if (strategyId == StrategyId.SLOW_DOWN) {
			return "오 쉬는 날이면 제대로 쉬어야지 ㅋㅋ 오늘은 그냥 편하게 보내";
		}
		return "오 좋다 ㅋㅋ 오늘은 편하게 쉬는 날이네. 뭐하면서 쉬고 있어?";
	}

	private List<String> alternativeReplies(StrategyId strategyId) {
		if (strategyId == StrategyId.MAKE_PLAN) {
			return List.of(
					"집에서 쉬는 날 좋지 ㅋㅋ 나는 이런 날 맛있는 거 먹으면 바로 회복돼",
					"그럼 오늘은 무리하지 말고 쉬어 ㅋㅋ 나중에 컨디션 좋을 때 맛있는 거 먹자"
			);
		}
		return List.of(
				"오 그렇구나 ㅋㅋ 오늘은 편하게 보내는 중이네",
				"쉬는 날이면 좋지 ㅋㅋ 뭐하면서 쉬는 게 제일 좋아?"
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
