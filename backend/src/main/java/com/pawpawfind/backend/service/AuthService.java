package com.pawpawfind.backend.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.pawpawfind.backend.dto.AuthResponse;
import com.pawpawfind.backend.entity.User;
import com.pawpawfind.backend.repository.UserRepository;

/**
 * 카카오 OAuth 로그인. code → access_token → 유저 정보 → users 저장/조회.
 */
@Service
public class AuthService {

	private static final String KAKAO_PROVIDER = "KAKAO";

	@Value("${kakao.rest-api-key}")
	private String restApiKey;

	@Value("${kakao.redirect-uri}")
	private String redirectUri;

	private final UserRepository userRepository;
	private final JwtService jwtService;
	private final RestClient restClient = RestClient.create();

	public AuthService(UserRepository userRepository, JwtService jwtService) {
		this.userRepository = userRepository;
		this.jwtService = jwtService;
	}

	public AuthResponse kakaoLogin(String code) {
		if (code == null || code.isBlank()) {
			throw new IllegalArgumentException("카카오 authorization code가 필요합니다.");
		}

		String accessToken = requestAccessToken(code);
		Map<String, Object> me = requestKakaoUser(accessToken);

		String providerId = String.valueOf(me.get("id"));
		String nickname = extractNickname(me);

		User user = userRepository.findByProviderAndProviderId(KAKAO_PROVIDER, providerId)
				.map(existing -> {
					if (!nickname.equals(existing.getNickname())) {
						existing.setNickname(nickname);
						return userRepository.save(existing);
					}
					return existing;
				})
				.orElseGet(() -> {
					User newUser = new User();
					newUser.setProvider(KAKAO_PROVIDER);
					newUser.setProviderId(providerId);
					newUser.setNickname(nickname);
					return userRepository.save(newUser);
				});

		String token = jwtService.createToken(user.getUserId());
		return new AuthResponse(token, user.getUserId(), user.getNickname(), user.getProvider());
	}

	private String requestAccessToken(String code) {
		String body = "grant_type=authorization_code"
				+ "&client_id=" + encode(restApiKey)
				+ "&redirect_uri=" + encode(redirectUri)
				+ "&code=" + encode(code);

		Map<String, Object> tokenResponse = restClient.post()
				.uri("https://kauth.kakao.com/oauth/token")
				.header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
				.body(body)
				.retrieve()
				.body(Map.class);

		if (tokenResponse == null || tokenResponse.get("access_token") == null) {
			throw new IllegalArgumentException("카카오 access_token을 받지 못했습니다.");
		}

		return (String) tokenResponse.get("access_token");
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> requestKakaoUser(String accessToken) {
		Map<String, Object> me = restClient.get()
				.uri("https://kapi.kakao.com/v2/user/me")
				.header("Authorization", "Bearer " + accessToken)
				.retrieve()
				.body(Map.class);

		if (me == null || me.get("id") == null) {
			throw new IllegalArgumentException("카카오 사용자 정보를 받지 못했습니다.");
		}

		return me;
	}

	@SuppressWarnings("unchecked")
	private String extractNickname(Map<String, Object> me) {
		Object kakaoAccountObj = me.get("kakao_account");
		if (kakaoAccountObj instanceof Map<?, ?> kakaoAccount) {
			Object profileObj = kakaoAccount.get("profile");
			if (profileObj instanceof Map<?, ?> profile) {
				Object nickname = profile.get("nickname");
				if (nickname instanceof String nicknameStr && !nicknameStr.isBlank()) {
					return nicknameStr;
				}
			}
		}
		return "카카오사용자" + me.get("id");
	}

	private String encode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}
}
