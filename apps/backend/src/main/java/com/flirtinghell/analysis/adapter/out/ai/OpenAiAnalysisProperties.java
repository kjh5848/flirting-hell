package com.flirtinghell.analysis.adapter.out.ai;

import java.net.URI;

record OpenAiAnalysisProperties(
		String provider,
		URI baseUrl,
		String apiKey,
		String model
) {
}
