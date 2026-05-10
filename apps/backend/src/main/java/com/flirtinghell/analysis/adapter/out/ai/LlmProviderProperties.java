package com.flirtinghell.analysis.adapter.out.ai;

import java.net.URI;

record LlmProviderProperties(
		LlmProvider provider,
		URI baseUrl,
		String apiKey,
		String model
) {
	String requireApiKey() {
		if (apiKey == null || apiKey.isBlank()) {
			throw new IllegalStateException(provider.name().toLowerCase() + " API key is required.");
		}
		return apiKey;
	}
}
