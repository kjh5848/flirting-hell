package com.flirtinghell.operations.adapter.in.web;

import java.util.List;

import com.flirtinghell.operations.domain.FeatureFlagStore;
import com.flirtinghell.operations.domain.SafetyScanner;
import com.flirtinghell.shared.api.ApiResponse;
import com.flirtinghell.shared.api.RequestIds;
import com.flirtinghell.shared.error.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/// 어드민 콘솔 Phase 1 — 대시보드 지표 + 사용자 목록.
/// 접근은 SecurityConfig의 `/api/admin/**` = ROLE_ADMIN으로 보호된다.
/// 지금은 mock 집계 데이터(원문·식별정보 미포함). 실 집계 쿼리는 다음 단계.
@RestController
@RequestMapping("/api/admin")
public class AdminController {

	@Value("${flirting-hell.ai.provider:mock}")
	private String aiProvider;

	private final FeatureFlagStore featureFlags;

	public AdminController(FeatureFlagStore featureFlags) {
		this.featureFlags = featureFlags;
	}

	// Phase 2: 안전 규칙을 적용해볼 샘플 AI 출력(시드). 일부러 불량 1~2건을 넣어
	// 모더레이션 큐가 비지 않게 한다. 실제 라이브 turn 스캔은 후속 단계.
	private static final List<SampleOutput> SAMPLE_OUTPUTS = List.of(
			new SampleOutput("추천 답장 샘플 #1", "오 좋다 ㅋㅋ 오늘은 편하게 쉬어. 내일 얘기하자"),
			new SampleOutput("추천 답장 샘플 #2", "왜 답 안 해? 지금 당장 답장해줘"),
			new SampleOutput("추천 답장 샘플 #3", "나 때문에 그러는 거 알지? 너 아니면 안 돼"),
			new SampleOutput("추천 답장 샘플 #4", "천천히 생각해보고 편할 때 알려줘")
	);

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

	@GetMapping("/moderation")
	ApiResponse<ModerationList> moderation(HttpServletRequest request) {
		// 생성된 출력에만 안전 규칙 적용(원문 대화 미검사). 플래그된 것만 반환.
		List<ModerationFlag> flags = SAMPLE_OUTPUTS.stream()
				.map(sample -> new ModerationFlag(
						sample.source(),
						sample.text(),
						SafetyScanner.scan(sample.text())))
				.filter(flag -> !flag.rules().isEmpty())
				.toList();
		return ApiResponse.of(new ModerationList(flags), RequestIds.from(request));
	}

	@GetMapping("/llm")
	ApiResponse<LlmStatus> llm(HttpServletRequest request) {
		// provider 현황 + 품질 요약(기존 analysis-quality 픽스처 기반) + mock 비용.
		LlmStatus status = new LlmStatus(
				aiProvider,
				List.of(
						new QualityRow("mock", "안전 규칙 위반 0건 · 형식 100% 통과(픽스처 기준)"),
						new QualityRow("gemini/gpt/claude", "키 투입 후 test:backend:analysis-quality:real 로 비교")
				),
				new CostEstimate(0, "mock provider는 토큰 비용 0. 실 provider는 키 투입 후 집계.")
		);
		return ApiResponse.of(status, RequestIds.from(request));
	}

	@GetMapping("/flags")
	ApiResponse<List<FeatureFlagStore.Flag>> flags(HttpServletRequest request) {
		return ApiResponse.of(featureFlags.all(), RequestIds.from(request));
	}

	@PatchMapping("/flags/{key}")
	ApiResponse<List<FeatureFlagStore.Flag>> setFlag(
			@PathVariable String key,
			@Valid @RequestBody FlagRequest body,
			HttpServletRequest request
	) {
		if (!featureFlags.set(key, body.enabled())) {
			throw new ResourceNotFoundException("FLAG_NOT_FOUND", "기능 플래그를 찾을 수 없습니다.");
		}
		return ApiResponse.of(featureFlags.all(), RequestIds.from(request));
	}

	@GetMapping("/announcements")
	ApiResponse<AnnouncementList> announcements(HttpServletRequest request) {
		List<Announcement> items = List.of(
				new Announcement("a1", "v0.2 분석 품질 개선", "추천 답장 톤 조절과 코치 대화를 추가했어요.", "2026-06-18"),
				new Announcement("a2", "개인정보 안내", "대화 원문은 저장하지 않습니다. 실명·연락처는 지우고 붙여넣어 주세요.", "2026-06-10")
		);
		return ApiResponse.of(new AnnouncementList(items), RequestIds.from(request));
	}

	@GetMapping("/revenue")
	ApiResponse<RevenueMetrics> revenue(HttpServletRequest request) {
		// 결제 연동(RevenueCat/AdMob) 전까지 mock 지표.
		RevenueMetrics revenue = new RevenueMetrics(
				1284000,  // mrrKrw
				86,       // payingUsers
				6,        // conversionRatePercent
				List.of(
						new PackageSales("스타터 분석권", 52, 202800),
						new PackageSales("집중 분석권", 34, 506600)
				),
				3         // refundsThisMonth
		);
		return ApiResponse.of(revenue, RequestIds.from(request));
	}

	public record FlagRequest(@NotNull Boolean enabled) {
	}

	public record AnnouncementList(List<Announcement> items) {
	}

	public record Announcement(String id, String title, String body, String publishedAt) {
	}

	public record RevenueMetrics(
			int mrrKrw,
			int payingUsers,
			int conversionRatePercent,
			List<PackageSales> byPackage,
			int refundsThisMonth
	) {
	}

	public record PackageSales(String name, int count, int revenueKrw) {
	}

	private record SampleOutput(String source, String text) {
	}

	public record ModerationList(List<ModerationFlag> flags) {
	}

	public record ModerationFlag(String source, String generatedText, List<String> rules) {
	}

	public record LlmStatus(String provider, List<QualityRow> quality, CostEstimate cost) {
	}

	public record QualityRow(String target, String note) {
	}

	public record CostEstimate(int monthlyKrw, String note) {
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
