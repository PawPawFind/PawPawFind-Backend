package com.pawpawfind.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pawpawfind.backend.entity.Reports;

/** 사용자 실종/목격 제보. */
public interface ReportRepository extends JpaRepository<Reports, Long> {
}
