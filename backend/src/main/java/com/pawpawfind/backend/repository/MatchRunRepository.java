package com.pawpawfind.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pawpawfind.backend.entity.MatchRun;

public interface MatchRunRepository extends JpaRepository<MatchRun, Long> {

	Optional<MatchRun> findTopByReportIdAndStatusOrderByCreatedAtDesc(Long reportId, String status);
}
