package com.flirtinghell.shared.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, FirebaseAuthenticationFilter firebaseAuthenticationFilter) throws Exception {
		return http
				.csrf(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.exceptionHandling(exception -> exception.authenticationEntryPoint(
						(request, response, authException) -> response.sendError(401, "Authentication required.")
				))
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.addFilterBefore(firebaseAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
				.authorizeHttpRequests(auth -> auth
							.requestMatchers(HttpMethod.GET, "/api/health", "/actuator/health/**").permitAll()
							.requestMatchers(HttpMethod.GET, "/actuator/info").permitAll()
							.requestMatchers(HttpMethod.POST, "/api/auth/kakao/exchange").permitAll()
							.anyRequest().authenticated()
					)
				.build();
	}
}
