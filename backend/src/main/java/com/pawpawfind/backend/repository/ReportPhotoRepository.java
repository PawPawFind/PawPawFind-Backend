package com.pawpawfind.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.pawpawfind.backend.entity.ReportPhotos;

/** 제보 사진. reportId 조건 조회는 아직 없음. */
public interface ReportPhotoRepository extends JpaRepository<ReportPhotos, Long> {
    List<ReportPhotos> findByReportId(Long reportId);

    void deleteByReportId(Long reportId);

    long countByReportId(Long reportId);

	@Query("""
			SELECT p FROM ReportPhotos p
			WHERE NOT EXISTS (
				SELECT 1 FROM ReportEmbedding e WHERE e.reportPhotoId = p.id
			)
			ORDER BY p.id ASC
			""")
	List<ReportPhotos> findPhotosMissingEmbeddings();
}


