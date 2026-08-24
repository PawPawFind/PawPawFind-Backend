package com.pawpawfind.backend.dto;

/**
 * S3 presigned PUT URL 발급 응답.
 * FE는 uploadUrl로 PUT 후 photoUrl을 POST /api/report-photos에 전달한다.
 */
public class PresignUploadResponse {

	private String uploadUrl;
	private String photoUrl;
	private String objectKey;

	public PresignUploadResponse(String uploadUrl, String photoUrl, String objectKey) {
		this.uploadUrl = uploadUrl;
		this.photoUrl = photoUrl;
		this.objectKey = objectKey;
	}

	public String getUploadUrl() {
		return uploadUrl;
	}

	public String getPhotoUrl() {
		return photoUrl;
	}

	public String getObjectKey() {
		return objectKey;
	}
}
