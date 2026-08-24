package com.pawpawfind.backend.dto;

/**
 * S3 presigned PUT URL 발급 요청.
 */
public class PresignUploadRequest {

	private String filename;
	private String contentType;

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
}
