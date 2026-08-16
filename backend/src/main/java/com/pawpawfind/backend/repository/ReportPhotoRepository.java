package com.pawpawfind.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pawpawfind.backend.entity.ReportPhotos;

/** 제보 사진. reportId 조건 조회는 아직 없음. */
public interface ReportPhotoRepository extends JpaRepository<ReportPhotos, Long> {
}
