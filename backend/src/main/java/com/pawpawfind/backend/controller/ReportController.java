package com.pawpawfind.backend.controller;

import com.pawpawfind.backend.config.JwtAuthFilter;
import com.pawpawfind.backend.dto.ReportListItemResponse;
import com.pawpawfind.backend.entity.ReportPhotos;
import com.pawpawfind.backend.entity.ReportFeatures;
import com.pawpawfind.backend.entity.Reports;
import com.pawpawfind.backend.service.ReportService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * 실종(LOST) / 목격(FOUND) 제보 API.
 * 생성(제보·사진·특징)은 비로그인 가능. 수정/삭제는 본인 또는 ADMIN만 가능.
 */
@RestController
public class ReportController{
    private final ReportService reportService;

    public ReportController(ReportService reportService){
        this.reportService = reportService;
    }

    // --- 제보 ---

    @PostMapping("/api/reports")
    public ResponseEntity<Reports> createReport(
            RequestEntity<Reports> requestEntity,
            @RequestAttribute(value = JwtAuthFilter.USER_ID_ATTR, required = false) Long userId) {
        Reports createdReport = reportService.createReport(requestEntity.getBody(), userId);
        return ResponseEntity.ok(createdReport);
    }

    @GetMapping("/api/reports/me")
    public ResponseEntity<Page<ReportListItemResponse>> getMyReports(
            @RequestAttribute(value = JwtAuthFilter.USER_ID_ATTR, required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(reportService.getMyReports(userId, pageable));
    }

    @GetMapping("/api/reports")
    public ResponseEntity<Page<ReportListItemResponse>> getReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String reportType) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(reportService.getReports(reportType, pageable));
    }

    // --- 사진 (URL만 저장) ---

    @PostMapping("/api/report-photos")
    public ResponseEntity<?> createReportPhoto(RequestEntity<ReportPhotos> requestEntity) {
        try {
            ReportPhotos createdReportPhoto = reportService.createReportPhoto(requestEntity.getBody());
            return ResponseEntity.ok(createdReportPhoto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/api/reports/{reportId}")
    public ResponseEntity<Reports> getReport(@PathVariable Long reportId){
        Reports report = reportService.getReport(reportId);
        if (report == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(report);
    }

    @GetMapping("/api/report-photos")
    public ResponseEntity<List<ReportPhotos>> getReportPhotos(@RequestParam Long reportId){
        List<ReportPhotos> reportPhotos = reportService.getReportPhotos(reportId);
        return ResponseEntity.ok(reportPhotos);
    }

    @GetMapping("/api/report-photos/{reportPhotoId}")
    public ResponseEntity<ReportPhotos> getReportPhoto(@PathVariable Long reportPhotoId){
        ReportPhotos reportPhoto = reportService.getReportPhoto(reportPhotoId);
        if (reportPhoto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(reportPhoto);
    }

    @DeleteMapping("/api/reports/{reportId}")
    public ResponseEntity<Void> deleteReport(
            @PathVariable Long reportId,
            @RequestAttribute(value = JwtAuthFilter.USER_ID_ATTR, required = false) Long userId,
            @RequestAttribute(value = JwtAuthFilter.ROLE_ATTR, required = false) String role) {
        reportService.assertCanManageReport(reportId, userId, role);
        reportService.deleteReport(reportId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/report-photos/{reportPhotoId}")
    public ResponseEntity<Void> deleteReportPhoto(
            @PathVariable Long reportPhotoId,
            @RequestAttribute(value = JwtAuthFilter.USER_ID_ATTR, required = false) Long userId,
            @RequestAttribute(value = JwtAuthFilter.ROLE_ATTR, required = false) String role) {
        reportService.assertCanManageReportPhoto(reportPhotoId, userId, role);
        reportService.deleteReportPhoto(reportPhotoId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/api/reports/{reportId}")
    public ResponseEntity<Reports> updateReport(
            @PathVariable Long reportId,
            RequestEntity<Reports> requestEntity,
            @RequestAttribute(value = JwtAuthFilter.USER_ID_ATTR, required = false) Long userId,
            @RequestAttribute(value = JwtAuthFilter.ROLE_ATTR, required = false) String role) {
        reportService.assertCanManageReport(reportId, userId, role);
        Reports report = requestEntity.getBody();
        Reports updatedReport = reportService.updateReport(reportId, report);
        if (updatedReport == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedReport);
    }

    @PutMapping("/api/report-photos/{reportPhotoId}")
    public ResponseEntity<ReportPhotos> updateReportPhoto(
            @PathVariable Long reportPhotoId,
            RequestEntity<ReportPhotos> requestEntity,
            @RequestAttribute(value = JwtAuthFilter.USER_ID_ATTR, required = false) Long userId,
            @RequestAttribute(value = JwtAuthFilter.ROLE_ATTR, required = false) String role) {
        reportService.assertCanManageReportPhoto(reportPhotoId, userId, role);
        ReportPhotos reportPhoto = requestEntity.getBody();
        ReportPhotos updatedReportPhoto = reportService.updateReportPhoto(reportPhotoId, reportPhoto);
        if (updatedReportPhoto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedReportPhoto);
    }

    // --- 특징 태그 ---

    @PostMapping("/api/report-features")
    public ResponseEntity<?> createReportFeature(RequestEntity<ReportFeatures> requestEntity) {
        try {
            ReportFeatures created = reportService.createReportFeature(requestEntity.getBody());
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/api/report-features")
    public ResponseEntity<List<ReportFeatures>> getReportFeatures(@RequestParam Long reportId) {
        return ResponseEntity.ok(reportService.getReportFeatures(reportId));
    }

    @GetMapping("/api/report-features/{reportFeatureId}")
    public ResponseEntity<ReportFeatures> getReportFeature(@PathVariable Long reportFeatureId) {
        ReportFeatures reportFeature = reportService.getReportFeature(reportFeatureId);
        if (reportFeature == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(reportFeature);
    }

    @PutMapping("/api/report-features/{reportFeatureId}")
    public ResponseEntity<ReportFeatures> updateReportFeature(
            @PathVariable Long reportFeatureId,
            RequestEntity<ReportFeatures> requestEntity,
            @RequestAttribute(value = JwtAuthFilter.USER_ID_ATTR, required = false) Long userId,
            @RequestAttribute(value = JwtAuthFilter.ROLE_ATTR, required = false) String role) {
        reportService.assertCanManageReportFeature(reportFeatureId, userId, role);
        ReportFeatures updated = reportService.updateReportFeature(reportFeatureId, requestEntity.getBody());
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/api/report-features/{reportFeatureId}")
    public ResponseEntity<Void> deleteReportFeature(
            @PathVariable Long reportFeatureId,
            @RequestAttribute(value = JwtAuthFilter.USER_ID_ATTR, required = false) Long userId,
            @RequestAttribute(value = JwtAuthFilter.ROLE_ATTR, required = false) String role) {
        reportService.assertCanManageReportFeature(reportFeatureId, userId, role);
        reportService.deleteReportFeature(reportFeatureId);
        return ResponseEntity.noContent().build();
    }
}
