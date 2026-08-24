package com.pawpawfind.backend.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.pawpawfind.backend.dto.AuthResponse;
import com.pawpawfind.backend.service.AuthService;

@RestController
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/api/auth/kakao")
	public ResponseEntity<AuthResponse> kakaoLogin(@RequestBody Map<String, String> body) {
		return ResponseEntity.ok(authService.kakaoLogin(body.get("code")));
	}
}
