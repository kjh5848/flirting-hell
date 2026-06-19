package com.flirtinghell.operations.domain;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

/// 어드민 기능 플래그(Phase 3, mock skeleton). in-memory — 재시작 시 기본값으로.
/// 실 영속화는 후속(추가형 테이블). 단일 소스에서 라벨·기본값을 정의한다.
@Component
public class FeatureFlagStore {

	public record Flag(String key, String label, boolean enabled) {
	}

	private static final List<Flag> DEFAULTS = List.of(
			new Flag("chat_coaching", "챗 코칭", true),
			new Flag("date_plan", "데이트 플랜", true),
			new Flag("real_place_recommendation", "실장소 추천", false),
			new Flag("reward_ads", "리워드 광고", false)
	);

	private final ConcurrentMap<String, Boolean> state = new ConcurrentHashMap<>();
	private final Map<String, String> labels = new ConcurrentHashMap<>();

	public FeatureFlagStore() {
		for (Flag flag : DEFAULTS) {
			state.put(flag.key(), flag.enabled());
			labels.put(flag.key(), flag.label());
		}
	}

	public List<Flag> all() {
		return DEFAULTS.stream()
				.map(flag -> new Flag(flag.key(), flag.label(), state.getOrDefault(flag.key(), flag.enabled())))
				.toList();
	}

	/// 존재하는 키만 설정한다. 알 수 없는 키는 무시(false 반환).
	public boolean set(String key, boolean enabled) {
		if (!labels.containsKey(key)) {
			return false;
		}
		state.put(key, enabled);
		return true;
	}
}
