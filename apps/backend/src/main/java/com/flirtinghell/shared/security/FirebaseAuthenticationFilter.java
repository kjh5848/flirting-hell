package com.flirtinghell.shared.security;

import java.io.IOException;
import java.util.List;

import com.flirtinghell.identity.application.port.out.FirebaseTokenVerifier;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
class FirebaseAuthenticationFilter extends OncePerRequestFilter {

	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String BEARER_PREFIX = "Bearer ";

	private final FirebaseTokenVerifier tokenVerifier;

	FirebaseAuthenticationFilter(FirebaseTokenVerifier tokenVerifier) {
		this.tokenVerifier = tokenVerifier;
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {
		String authorization = request.getHeader(AUTHORIZATION_HEADER);

		if (authorization != null && authorization.startsWith(BEARER_PREFIX)) {
			String token = authorization.substring(BEARER_PREFIX.length());
			AuthenticatedUser user;
			try {
				user = tokenVerifier.verify(token);
			} catch (BadCredentialsException exception) {
				response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid bearer token.");
				return;
			}
			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
					user,
					token,
					List.of(new SimpleGrantedAuthority("ROLE_USER"))
			);
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}

		try {
			filterChain.doFilter(request, response);
		} finally {
			SecurityContextHolder.clearContext();
		}
	}
}
