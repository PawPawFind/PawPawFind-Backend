package com.pawpawfind.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.pawpawfind.backend.entity.Reports;

/** 사용자 실종/목격 제보. */
public interface ReportRepository extends JpaRepository<Reports, Long> {

	Page<Reports> findAllByOrderByCreatedAtDesc(Pageable pageable);

	Page<Reports> findByReportTypeOrderByCreatedAtDesc(String reportType, Pageable pageable);

	Page<Reports> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
