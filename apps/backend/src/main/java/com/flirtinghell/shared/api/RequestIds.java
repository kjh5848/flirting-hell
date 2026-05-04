package com.flirtinghell.shared.api;

import jakarta.servlet.http.HttpServletRequest;

public final class RequestIds {

	public static final String HEADER_NAME = "X-Request-Id";
	public static final String ATTRIBUTE_NAME = "flirtingHell.requestId";

	private RequestIds() {
	}

	public static String from(HttpServletRequest request) {
		Object value = request.getAttribute(ATTRIBUTE_NAME);
		if (value instanceof String requestId && !requestId.isBlank()) {
			return requestId;
		}
		return "unknown";
	}
}
