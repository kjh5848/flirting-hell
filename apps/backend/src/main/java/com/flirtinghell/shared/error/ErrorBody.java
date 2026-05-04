package com.flirtinghell.shared.error;

import java.util.Map;

public record ErrorBody(
		String code,
		String message,
		String requestId,
		Map<String, Object> details
) {
}
