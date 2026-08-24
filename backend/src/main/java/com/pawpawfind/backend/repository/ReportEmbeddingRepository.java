package com.pawpawfind.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pawpawfind.backend.entity.ReportEmbedding;

public interface ReportEmbeddingRepository extends JpaRepository<ReportEmbedding, Long> {

	ReportEmbedding findByReportPhotoId(Long reportPhotoId);

	void deleteByReportPhotoId(Long reportPhotoId);

	void deleteByReportId(Long reportId);

	@Query("""
			SELECT re FROM ReportEmbedding re
			JOIN Reports r ON r.reportId = re.reportId
			WHERE re.modelVersion = :modelVersion
			  AND re.preprocessVersion = :preprocessVersion
			  AND r.species = :speciesKo
			  AND (:excludeReportId IS NULL OR re.reportId <> :excludeReportId)
			""")
	List<ReportEmbedding> findForGallerySearch(
			@Param("speciesKo") String speciesKo,
			@Param("modelVersion") String modelVersion,
			@Param("preprocessVersion") String preprocessVersion,
			@Param("excludeReportId") Long excludeReportId);
}
