package com.pawpawfind.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.pawpawfind.backend.dto.PresignUploadRequest;
import com.pawpawfind.backend.dto.PresignUploadResponse;
import com.pawpawfind.backend.service.S3UploadService;

/**
 * 제보 사진 S3 업로드용 presigned URL 발급.
 */
@RestController
public class UploadController {

	private final S3UploadService s3UploadService;

	public UploadController(S3UploadService s3UploadService) {
		this.s3UploadService = s3UploadService;
	}

	@PostMapping("/api/uploads/presign")
	public ResponseEntity<?> createPresignedUpload(@RequestBody PresignUploadRequest request) {
		try {
			PresignUploadResponse response = s3UploadService.createPresignedUpload(request);
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}
}
