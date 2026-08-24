package com.pawpawfind.backend.config;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.pawpawfind.backend.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Authorization: Bearer 토큰이 있으면 userId를 request attribute에 넣는다.
 * 토큰이 없거나 잘못됐어도 요청은 통과시킨다(익명 API 허용).
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

	public static final String USER_ID_ATTR = "userId";

	private final JwtService jwtService;

	public JwtAuthFilter(JwtService jwtService) {
		this.jwtService = jwtService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String auth = request.getHeader("Authorization");
		if (auth != null && auth.startsWith("Bearer ")) {
			try {
				Long userId = jwtService.parseUserId(auth.substring(7).trim());
				request.setAttribute(USER_ID_ATTR, userId);
			} catch (RuntimeException ignored) {
				// invalid token — treat as anonymous
			}
		}
		chain.doFilter(request, response);
	}
}
