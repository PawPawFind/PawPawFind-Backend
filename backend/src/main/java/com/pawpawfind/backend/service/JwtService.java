package com.pawpawfind.backend.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.pawpawfind.backend.entity.UserRoles;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/** 앱 JWT 발급·검증. subject = userId, claim role. */
@Service
public class JwtService {

	private static final String ROLE_CLAIM = "role";

	@Value("${jwt.secret}")
	private String secret;

	@Value("${jwt.expiration-ms:86400000}")
	private long expirationMs;

	private SecretKey signingKey() {
		return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	public String createToken(Long userId, String role) {
		Date now = new Date();
		String effectiveRole = (role == null || role.isBlank()) ? UserRoles.USER : role;
		return Jwts.builder()
				.subject(String.valueOf(userId))
				.claim(ROLE_CLAIM, effectiveRole)
				.issuedAt(now)
				.expiration(new Date(now.getTime() + expirationMs))
				.signWith(signingKey())
				.compact();
	}

	public Long parseUserId(String token) {
		return Long.parseLong(parseClaims(token).getSubject());
	}

	public String parseRole(String token) {
		Object role = parseClaims(token).get(ROLE_CLAIM);
		if (role instanceof String roleStr && !roleStr.isBlank()) {
			return roleStr;
		}
		return UserRoles.USER;
	}

	private Claims parseClaims(String token) {
		return Jwts.parser()
				.verifyWith(signingKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
}
