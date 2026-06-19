package com.flirtinghell.operations.adapter.in.web;

import java.util.List;

import com.flirtinghell.shared.api.ApiResponse;
import com.flirtinghell.shared.api.RequestIds;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/// 어드민 콘솔 Phase 1 — 대시보드 지표 + 사용자 목록.
/// 접근은 SecurityConfig의 `/api/admin/**` = ROLE_ADMIN으로 보호된다.
/// 지금은 mock 집계 데이터(원문·식별정보 미포함). 실 집계 쿼리는 다음 단계.
@RestController
@RequestMapping("/api/admin")
public class AdminController {

	@GetMapping("/metrics")
	ApiResponse<AdminMetrics> metrics(HttpServletRequest request) {
		AdminMetrics metrics = new AdminMetrics(
				128,   // dailyActiveUsers
				342,   // analysesToday
				47,    // savedReplies
				new OutcomeBreakdown(58, 24, 18), // sentGood / sentSoso / notSent (%)
				new CreditUsage(1024, 256, 88)    // freeUsedToday / creditSpentToday / rewardWatched
		);
		return ApiResponse.of(metrics, RequestIds.from(request));
	}

	@GetMapping("/users")
	ApiResponse<AdminUserList> users(HttpServletRequest request) {
		List<AdminUserSummary> users = List.of(
				new AdminUserSummary("usr_8f2a", true, 12, 3, "2026-06-10"),
				new AdminUserSummary("usr_3c91", true, 5, 1, "2026-06-12"),
				new AdminUserSummary("usr_a7d0", false, 0, 3, "2026-06-18")
		);
		return ApiResponse.of(new AdminUserList(users), RequestIds.from(request));
	}

	// 비식별 집계만 노출(원문·실명 없음).
	public record AdminMetrics(
			int dailyActiveUsers,
			int analysesToday,
			int savedReplies,
			OutcomeBreakdown outcome,
			CreditUsage credit
	) {
	}

	public record OutcomeBreakdown(int sentGood, int sentSoso, int notSent) {
	}

	public record CreditUsage(int freeUsedToday, int creditSpentToday, int rewardWatched) {
	}

	public record AdminUserList(List<AdminUserSummary> users) {
	}

	public record AdminUserSummary(
			String userId,
			boolean onboardingCompleted,
			int analysisCount,
			int freeRemaining,
			String joinedAt
	) {
	}
}
