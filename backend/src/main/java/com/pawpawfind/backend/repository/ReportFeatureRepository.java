package com.pawpawfind.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pawpawfind.backend.entity.ReportFeatures;

/** 제보 특징 태그. findByReportId 로 해당 제보만 조회. */
public interface ReportFeatureRepository extends JpaRepository<ReportFeatures, Long> {

	List<ReportFeatures> findByReportId(Long reportId);
}
