package com.pawpawfind.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pawpawfind.backend.entity.User;

/** 소셜 로그인 사용자. */
public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByProviderAndProviderId(String provider, String providerId);
}
