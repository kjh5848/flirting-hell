package com.flirtinghell.analysis.adapter.out.ai;

import java.net.URI;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LlmClientFactoryTest {

	private final LlmClientFactory factory = new LlmClientFactory(new ObjectMapper());

	@Test
	void createsProviderClientFromConfiguredProvider() {
		assertThat(factory.create(properties("gpt"))).isInstanceOf(GptLlmClient.class);
		assertThat(factory.create(properties("openai"))).isInstanceOf(GptLlmClient.class);
		assertThat(factory.create(properties("gemini"))).isInstanceOf(GeminiLlmClient.class);
		assertThat(factory.create(properties("claude"))).isInstanceOf(ClaudeLlmClient.class);
		assertThat(factory.create(properties("anthropic"))).isInstanceOf(ClaudeLlmClient.class);
	}

	private static LlmProviderProperties properties(String provider) {
		return new LlmProviderProperties(
				LlmProvider.from(provider),
				URI.create("http://127.0.0.1"),
				"test-key",
				provider + "-model"
		);
	}
}
