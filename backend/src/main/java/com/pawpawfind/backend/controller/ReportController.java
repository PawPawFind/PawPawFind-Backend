package com.pawpawfind.backend.controller;

import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import com.pawpawfind.backend.entity.ReportPhotos;
import com.pawpawfind.backend.entity.ReportFeatures;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import com.pawpawfind.backend.service.ReportService;
import com.pawpawfind.backend.entity.Reports;
import java.util.List;

import org.springframework.web.bind.annotation.RestController;

/**
 * 실종(LOST) / 목격(FOUND) 제보 API.
 * 사진·특징 태그는 제보 ID를 만든 뒤 별도 테이블에 저장한다.
 */
@RestController
public class ReportController{
    private final ReportService reportService;

    public ReportController(ReportService reportService){
        this.reportService = reportService;
    }

    // --- 제보 ---

    @PostMapping("/api/reports")
    public ResponseEntity<Reports> createReport(RequestEntity<Reports> requestEntity){
        Reports report = requestEntity.getBody();
        Reports createdReport = reportService.createReport(report);
        return ResponseEntity.ok(createdReport);
    }

    // --- 사진 (URL만 저장) ---

    @PostMapping("/api/report-photos")
    public ResponseEntity<ReportPhotos> createReportPhoto(RequestEntity<ReportPhotos> requestEntity){
        ReportPhotos reportPhoto = requestEntity.getBody();
        ReportPhotos createdReportPhoto = reportService.createReportPhoto(reportPhoto);
        return ResponseEntity.ok(createdReportPhoto);
    }

    @GetMapping("/api/reports")
    public ResponseEntity<List<Reports>> getReports(){
        List<Reports> reports = reportService.getReports();
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/api/reports/{reportId}")
    public ResponseEntity<Reports> getReport(@PathVariable Long reportId){
        Reports report = reportService.getReport(reportId);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/api/report-photos")
    public ResponseEntity<List<ReportPhotos>> getReportPhotos(Long reportId){
        List<ReportPhotos> reportPhotos = reportService.getReportPhotos(reportId);
        return ResponseEntity.ok(reportPhotos);
    }

    @GetMapping("/api/report-photos/{reportPhotoId}")
    public ResponseEntity<ReportPhotos> getReportPhoto(@PathVariable Long reportPhotoId){
        ReportPhotos reportPhoto = reportService.getReportPhoto(reportPhotoId);
        return ResponseEntity.ok(reportPhoto);
    }   

    @DeleteMapping("/api/reports/{reportId}")
    public ResponseEntity<Void> deleteReport(@PathVariable Long reportId){
        reportService.deleteReport(reportId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/report-photos/{reportPhotoId}")
    public ResponseEntity<Void> deleteReportPhoto(@PathVariable Long reportPhotoId){
        reportService.deleteReportPhoto(reportPhotoId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/api/reports/{reportId}")
    public ResponseEntity<Reports> updateReport(@PathVariable Long reportId, RequestEntity<Reports> requestEntity){
        Reports report = requestEntity.getBody();
        Reports updatedReport = reportService.updateReport(reportId, report);
        return ResponseEntity.ok(updatedReport);
    }

    @PutMapping("/api/report-photos/{reportPhotoId}")
    public ResponseEntity<ReportPhotos> updateReportPhoto(@PathVariable Long reportPhotoId, RequestEntity<ReportPhotos> requestEntity){
        ReportPhotos reportPhoto = requestEntity.getBody();
        ReportPhotos updatedReportPhoto = reportService.updateReportPhoto(reportPhotoId, reportPhoto);
        return ResponseEntity.ok(updatedReportPhoto);
    }

    // --- 특징 태그 ---

    @PostMapping("/api/report-features")
    public ResponseEntity<ReportFeatures> createReportFeature(RequestEntity<ReportFeatures> requestEntity) {
        ReportFeatures created = reportService.createReportFeature(requestEntity.getBody());
        return ResponseEntity.ok(created);
    }

    @GetMapping("/api/report-features")
    public ResponseEntity<List<ReportFeatures>> getReportFeatures(@RequestParam Long reportId) {
        return ResponseEntity.ok(reportService.getReportFeatures(reportId));
    }

    @GetMapping("/api/report-features/{reportFeatureId}")
    public ResponseEntity<ReportFeatures> getReportFeature(@PathVariable Long reportFeatureId) {
        return ResponseEntity.ok(reportService.getReportFeature(reportFeatureId));
    }

    @PutMapping("/api/report-features/{reportFeatureId}")
    public ResponseEntity<ReportFeatures> updateReportFeature(
            @PathVariable Long reportFeatureId,
            RequestEntity<ReportFeatures> requestEntity) {
        ReportFeatures updated = reportService.updateReportFeature(reportFeatureId, requestEntity.getBody());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/api/report-features/{reportFeatureId}")
    public ResponseEntity<Void> deleteReportFeature(@PathVariable Long reportFeatureId) {
        reportService.deleteReportFeature(reportFeatureId);
        return ResponseEntity.noContent().build();
    }
}