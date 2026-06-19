package com.flirtinghell.operations.domain;

import java.util.List;

/// AI가 생성한 텍스트(추천 답장 등)에 안전 규칙을 결정적으로 적용한다.
/// 원문 대화가 아니라 **생성된 출력만** 검사한다(프라이버시 원칙).
public final class SafetyScanner {

	private SafetyScanner() {
	}

	/// 매칭된 규칙 id 목록(없으면 빈 리스트). 순수 함수 → 단위 테스트로 고정.
	public static List<String> scan(String generatedText) {
		if (generatedText == null || generatedText.isBlank()) {
			return List.of();
		}
		return SafetyRule.RULES.stream()
				.filter(rule -> rule.patterns().stream().anyMatch(generatedText::contains))
				.map(SafetyRule::id)
				.toList();
	}
}
