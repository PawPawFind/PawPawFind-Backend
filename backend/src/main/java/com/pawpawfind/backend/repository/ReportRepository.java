package com.pawpawfind.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import com.pawpawfind.backend.entity.Reports;

/** 사용자 실종/목격 제보. */
public interface ReportRepository extends JpaRepository<Reports, Long> {

	Page<Reports> findAllByOrderByCreatedAtDesc(Pageable pageable);

	Page<Reports> findByReportTypeOrderByCreatedAtDesc(String reportType, Pageable pageable);

	Page<Reports> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

	@Query("""
			SELECT r FROM Reports r
			WHERE r.status = 'OPEN'
			AND r.latitude BETWEEN :minLatitude AND :maxLatitude
			AND r.longitude BETWEEN :minLongitude AND :maxLongitude
			AND (:reportType IS NULL OR r.reportType = :reportType)
			AND (:species IS NULL OR r.species = :species)
			""")
	List<Reports> findNearbyCandidates(
			@Param("minLatitude") double minLatitude,
			@Param("maxLatitude") double maxLatitude,
			@Param("minLongitude") double minLongitude,
			@Param("maxLongitude") double maxLongitude,
			@Param("reportType") String reportType,
			@Param("species") String species);

	@Query("""
			SELECT r FROM Reports r
			WHERE r.status = 'OPEN'
			AND r.latitude BETWEEN :minLatitude AND :maxLatitude
			AND (r.longitude >= :minLongitude OR r.longitude <= :maxLongitude)
			AND (:reportType IS NULL OR r.reportType = :reportType)
			AND (:species IS NULL OR r.species = :species)
			""")
	List<Reports> findNearbyCandidatesAcrossDateLine(
			@Param("minLatitude") double minLatitude,
			@Param("maxLatitude") double maxLatitude,
			@Param("minLongitude") double minLongitude,
			@Param("maxLongitude") double maxLongitude,
			@Param("reportType") String reportType,
			@Param("species") String species);
}
