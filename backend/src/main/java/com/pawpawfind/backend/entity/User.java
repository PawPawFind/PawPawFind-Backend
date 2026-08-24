package com.pawpawfind.backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * 소셜 로그인 사용자. 카카오/구글 OAuth 연동용.
 * role: USER(기본) / ADMIN(설정에 등록된 카카오 ID).
 */
@Entity
@Table(
	name = "users",
	uniqueConstraints = @UniqueConstraint(
		name = "uk_users_provider",
		columnNames = {"provider", "provider_id"}
	)
)
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Long userId;

	/** KAKAO 또는 GOOGLE */
	@Column(name = "provider", length = 20, nullable = false)
	private String provider;

	/** 카카오/구글 쪽 사용자 고유 ID */
	@Column(name = "provider_id", length = 100, nullable = false)
	private String providerId;

	@Column(name = "nickname", length = 50, nullable = false)
	private String nickname;

	/** USER 또는 ADMIN */
	@Column(name = "role", length = 20, nullable = false)
	private String role = UserRoles.USER;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@PrePersist
	public void onCreate() {
		LocalDateTime now = LocalDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
		if (this.role == null || this.role.isBlank()) {
			this.role = UserRoles.USER;
		}
	}

	@PreUpdate
	public void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getProviderId() {
		return providerId;
	}

	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
}
