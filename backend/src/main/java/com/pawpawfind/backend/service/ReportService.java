package com.pawpawfind.backend.service;

import org.springframework.stereotype.Service;
import com.pawpawfind.backend.repository.ReportRepository;
import com.pawpawfind.backend.repository.ReportPhotoRepository;
import com.pawpawfind.backend.repository.ReportFeatureRepository;
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

    private final ReportRepository reportRepository;
    private final ReportPhotoRepository reportPhotoRepository;
    private final ReportFeatureRepository reportFeatureRepository;

    public ReportService(ReportRepository reportRepository,
            ReportPhotoRepository reportPhotoRepository,
            ReportFeatureRepository reportFeatureRepository) {
        this.reportRepository = reportRepository;
        this.reportPhotoRepository = reportPhotoRepository;
        this.reportFeatureRepository = reportFeatureRepository;
    }

    public Reports createReport(Reports report){
        return reportRepository.save(report);
    }

    public ReportPhotos createReportPhoto(ReportPhotos reportPhoto){
        return reportPhotoRepository.save(reportPhoto);
    }

    public List<Reports> getReports(){
        return reportRepository.findAll();

    }

    public Reports getReport(Long reportId){
        return reportRepository.findById(reportId).orElse(null);

    }

    /** TODO: reportId로 필터해야 함. 지금은 전체 사진을 반환한다. */
    public List<ReportPhotos> getReportPhotos(Long reportId){
        return reportPhotoRepository.findAll();

    }

    public ReportPhotos getReportPhoto(Long reportPhotoId){
        return reportPhotoRepository.findById(reportPhotoId).orElse(null);
    }

    public void deleteReport(Long reportId){
        reportRepository.deleteById(reportId);
    }

    public void deleteReportPhoto(Long reportPhotoId){
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
        existingReport.setColor(report.getColor());
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


        return reportPhotoRepository.save(existingReportPhoto);
    }

    public ReportFeatures createReportFeature(ReportFeatures reportFeature) {
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