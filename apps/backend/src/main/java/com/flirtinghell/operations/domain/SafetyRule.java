package com.flirtinghell.operations.domain;

import java.util.List;

/// 안전 규칙 단일 소스(휴리스틱). "무엇을 조작/압박으로 볼지"는 운영자가 튜닝할 안전 정책이라
/// 한곳에 모은다. 주의: 이건 결정적 키워드 휴리스틱 = **placeholder 안전 신호**이지,
/// 모든 조작을 잡는 진짜 분류기가 아니다(실 LLM 안전 분류는 키 단계 후속).
public record SafetyRule(String id, String label, List<String> patterns) {

	public static final List<SafetyRule> RULES = List.of(
			new SafetyRule("PRESSURE", "압박·재촉", List.of(
					"당장 답", "빨리 답", "지금 바로 답", "왜 답 안", "왜 답장 안", "꼭 만나")),
			new SafetyRule("GUILT", "죄책감 유발", List.of(
					"나 때문에", "실망했", "서운하게", "너 아니면", "내가 이만큼")),
			new SafetyRule("OBSESSION", "집착·통제", List.of(
					"어디야 지금", "누구랑 있어", "다른 사람 만나지", "계속 연락"))
	);
}
