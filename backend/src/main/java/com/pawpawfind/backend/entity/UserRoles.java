package com.pawpawfind.backend.entity;

/**
 * 사용자 권한.
 * USER: 본인 제보만 수정/삭제.
 * ADMIN: 전체 제보 생성·수정·삭제.
 */
public final class UserRoles {

	public static final String USER = "USER";
	public static final String ADMIN = "ADMIN";

	private UserRoles() {
	}

	public static boolean isAdmin(String role) {
		return ADMIN.equals(role);
	}
}
