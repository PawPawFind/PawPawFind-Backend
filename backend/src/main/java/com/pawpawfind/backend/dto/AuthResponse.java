package com.pawpawfind.backend.dto;

/** 로그인 성공 응답. FE는 accessToken을 저장한다. */
public class AuthResponse {

	private String accessToken;
	private Long userId;
	private String nickname;
	private String provider;
	private String role;

	public AuthResponse() {
	}

	public AuthResponse(String accessToken, Long userId, String nickname, String provider, String role) {
		this.accessToken = accessToken;
		this.userId = userId;
		this.nickname = nickname;
		this.provider = provider;
		this.role = role;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
}
