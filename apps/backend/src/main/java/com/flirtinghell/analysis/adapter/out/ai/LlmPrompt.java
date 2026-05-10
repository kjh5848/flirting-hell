package com.flirtinghell.analysis.adapter.out.ai;

import java.util.Map;

record LlmPrompt(
		String systemInstructions,
		String userPrompt,
		Map<String, Object> responseSchema
) {
}
