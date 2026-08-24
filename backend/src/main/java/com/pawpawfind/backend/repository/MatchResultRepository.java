package com.pawpawfind.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pawpawfind.backend.entity.MatchResult;

public interface MatchResultRepository extends JpaRepository<MatchResult, Long> {

	List<MatchResult> findByMatchRunIdOrderByRankAsc(Long matchRunId);
}
