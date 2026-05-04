package com.flirtinghell.shared.api;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RequestIdFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {
		String requestId = resolveRequestId(request);
		request.setAttribute(RequestIds.ATTRIBUTE_NAME, requestId);
		response.setHeader(RequestIds.HEADER_NAME, requestId);
		MDC.put("requestId", requestId);

		try {
			filterChain.doFilter(request, response);
		} finally {
			MDC.remove("requestId");
		}
	}

	private String resolveRequestId(HttpServletRequest request) {
		String headerValue = request.getHeader(RequestIds.HEADER_NAME);
		if (headerValue != null && !headerValue.isBlank()) {
			return headerValue;
		}
		return "req_" + UUID.randomUUID();
	}
}
