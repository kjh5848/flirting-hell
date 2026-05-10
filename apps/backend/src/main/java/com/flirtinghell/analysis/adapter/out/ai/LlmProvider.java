package com.flirtinghell.analysis.adapter.out.ai;

enum LlmProvider {
	GPT,
	GEMINI,
	CLAUDE;

	static LlmProvider from(String value) {
		String normalized = value == null ? "" : value.trim().toLowerCase();
		return switch (normalized) {
			case "gpt", "openai" -> GPT;
			case "gemini", "google" -> GEMINI;
			case "claude", "anthropic" -> CLAUDE;
			default -> throw new IllegalArgumentException("Unsupported LLM provider: " + value);
		};
	}
}
