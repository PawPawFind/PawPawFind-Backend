package com.pawpawfind.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.pawpawfind.backend.repository.ReportRepository;
import com.pawpawfind.backend.repository.ReportPhotoRepository;
import com.pawpawfind.backend.repository.ReportFeatureRepository;
import com.pawpawfind.backend.repository.ReportEmbeddingRepository;
import com.pawpawfind.backend.entity.Reports;
import com.pawpawfind.backend.entity.ReportPhotos;
import com.pawpawfind.backend.entity.ReportFeatures;
import java.util.List;

/**
 * 제보 저장/조회/수정/삭제.
 * 사용자가 고치는 필드만 update 하고, reportId·createdAt 은 유지한다.
 */
@Service 
public class ReportService {

    private static final String COLOR_CATEGORY = "털색";
    private static final int MAX_COLORS_PER_REPORT = 3;

    private final ReportRepository reportRepository;
    private final ReportPhotoRepository reportPhotoRepository;
    private final ReportFeatureRepository reportFeatureRepository;
    private final ReportEmbeddingRepository reportEmbeddingRepository;
    private final ReportEmbedTriggerService reportEmbedTriggerService;

    public ReportService(ReportRepository reportRepository,
            ReportPhotoRepository reportPhotoRepository,
            ReportFeatureRepository reportFeatureRepository,
            ReportEmbeddingRepository reportEmbeddingRepository,
            ReportEmbedTriggerService reportEmbedTriggerService) {
        this.reportRepository = reportRepository;
        this.reportPhotoRepository = reportPhotoRepository;
        this.reportFeatureRepository = reportFeatureRepository;
        this.reportEmbeddingRepository = reportEmbeddingRepository;
        this.reportEmbedTriggerService = reportEmbedTriggerService;
    }

    public Reports createReport(Reports report, Long userId){
        if (userId != null) {
            report.setUserId(userId);
        }
        return reportRepository.save(report);
    }

    public ReportPhotos createReportPhoto(ReportPhotos reportPhoto){
        long count = reportPhotoRepository.countByReportId(reportPhoto.getReportId());
        if (count >= 3) {
            throw new IllegalArgumentException("사진은 제보당 최대 3장까지입니다.");
        }
        ReportPhotos saved = reportPhotoRepository.save(reportPhoto);
        reportEmbedTriggerService.triggerReportPhotoEmbed(saved);
        return saved;
    }

    public Page<Reports> getReports(String reportType, Pageable pageable) {
        if (reportType == null || reportType.isBlank()) {
            return reportRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        return reportRepository.findByReportTypeOrderByCreatedAtDesc(reportType, pageable);
    }

    public Page<Reports> getMyReports(Long userId, Pageable pageable) {
        return reportRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public Reports getReport(Long reportId){
        return reportRepository.findById(reportId).orElse(null);

    }

    public List<ReportPhotos> getReportPhotos(Long reportId){
        return reportPhotoRepository.findByReportId(reportId);
    }

    public ReportPhotos getReportPhoto(Long reportPhotoId){
        return reportPhotoRepository.findById(reportPhotoId).orElse(null);
    }

    public void deleteReport(Long reportId){
        reportEmbeddingRepository.deleteByReportId(reportId);
        reportPhotoRepository.deleteByReportId(reportId);
        reportFeatureRepository.deleteByReportId(reportId);
        reportRepository.deleteById(reportId);
    }

    public void deleteReportPhoto(Long reportPhotoId){
        reportEmbeddingRepository.deleteByReportPhotoId(reportPhotoId);
        reportPhotoRepository.deleteById(reportPhotoId);
    }


    public Reports updateReport(Long reportId, Reports report) {
        Reports existingReport = reportRepository.findById(reportId).orElse(null);
        if (existingReport == null) {
            return null;
        }

        existingReport.setTitle(report.getTitle());
        existingReport.setReportType(report.getReportType());
        existingReport.setSpecies(report.getSpecies());
        existingReport.setSize(report.getSize());
        existingReport.setEventDate(report.getEventDate());
        existingReport.setEventHour(report.getEventHour());
        existingReport.setHappenPlace(report.getHappenPlace());
        existingReport.setLatitude(report.getLatitude());
        existingReport.setLongitude(report.getLongitude());
        existingReport.setDescription(report.getDescription());
        existingReport.setStatus(report.getStatus());

        return reportRepository.save(existingReport);
    }

    public ReportPhotos updateReportPhoto(Long reportPhotoId, ReportPhotos reportPhoto) {
        ReportPhotos existingReportPhoto = reportPhotoRepository.findById(reportPhotoId).orElse(null);
        if (existingReportPhoto == null) {
            return null;
        }

        existingReportPhoto.setPhotoUrl(reportPhoto.getPhotoUrl());
        existingReportPhoto.setSortOrder(reportPhoto.getSortOrder());

        ReportPhotos saved = reportPhotoRepository.save(existingReportPhoto);
        reportEmbedTriggerService.triggerReportPhotoEmbed(saved);
        return saved;
    }

    public ReportFeatures createReportFeature(ReportFeatures reportFeature) {
        if (COLOR_CATEGORY.equals(reportFeature.getCategory())) {
            long count = reportFeatureRepository.countByReportIdAndCategory(
                    reportFeature.getReportId(), COLOR_CATEGORY);
            if (count >= MAX_COLORS_PER_REPORT) {
                throw new IllegalArgumentException("털색은 제보당 최대 3개까지입니다.");
            }
        }
        return reportFeatureRepository.save(reportFeature);
    }

    public List<ReportFeatures> getReportFeatures(Long reportId) {
        return reportFeatureRepository.findByReportId(reportId);
    }

    public ReportFeatures getReportFeature(Long reportFeatureId) {
        return reportFeatureRepository.findById(reportFeatureId).orElse(null);
    }

    public void deleteReportFeature(Long reportFeatureId) {
        reportFeatureRepository.deleteById(reportFeatureId);
    }

    public ReportFeatures updateReportFeature(Long reportFeatureId, ReportFeatures reportFeature) {
        ReportFeatures existing = reportFeatureRepository.findById(reportFeatureId).orElse(null);
        if (existing == null) {
            return null;
        }
        existing.setCategory(reportFeature.getCategory());
        existing.setKeyword(reportFeature.getKeyword());
        return reportFeatureRepository.save(existing);
    }
}