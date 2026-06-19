package com.flirtinghell.operations.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SafetyScannerTest {

	@Test
	void safeReplyHasNoFlags() {
		assertThat(SafetyScanner.scan("오 좋다 ㅋㅋ 편할 때 천천히 알려줘")).isEmpty();
		assertThat(SafetyScanner.scan(null)).isEmpty();
		assertThat(SafetyScanner.scan("")).isEmpty();
	}

	@Test
	void pressureAndGuiltAndObsessionAreFlagged() {
		assertThat(SafetyScanner.scan("왜 답 안 해? 지금 당장 답장해줘")).contains("PRESSURE");
		assertThat(SafetyScanner.scan("나 때문에 그러는 거 알지? 너 아니면 안 돼"))
				.contains("GUILT");
		assertThat(SafetyScanner.scan("지금 어디야 지금 누구랑 있어")).contains("OBSESSION");
	}
}
