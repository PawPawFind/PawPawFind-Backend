package com.pawpawfind.backend.service;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.pawpawfind.backend.dto.PresignUploadRequest;
import com.pawpawfind.backend.dto.PresignUploadResponse;

import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

/**
 * 제보 사진용 S3 presigned PUT URL 발급.
 */
@Service
public class S3UploadService {

	private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
			"image/jpeg",
			"image/png",
			"image/webp");

	private static final Map<String, String> EXTENSION_BY_CONTENT_TYPE = Map.of(
			"image/jpeg", ".jpg",
			"image/png", ".png",
			"image/webp", ".webp");

	private final S3Presigner s3Presigner;

	@Value("${aws.s3.bucket}")
	private String bucket;

	@Value("${aws.region}")
	private String region;

	@Value("${aws.s3.upload-prefix:reports/}")
	private String uploadPrefix;

	public S3UploadService(S3Presigner s3Presigner) {
		this.s3Presigner = s3Presigner;
	}

	public PresignUploadResponse createPresignedUpload(PresignUploadRequest request) {
		String contentType = normalizeContentType(request.getContentType());
		validateContentType(contentType);

		String extension = resolveExtension(request.getFilename(), contentType);
		String objectKey = uploadPrefix + UUID.randomUUID() + extension;

		PutObjectRequest putObjectRequest = PutObjectRequest.builder()
				.bucket(bucket)
				.key(objectKey)
				.contentType(contentType)
				.build();

		PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(
				builder -> builder
						.putObjectRequest(putObjectRequest)
						.signatureDuration(Duration.ofMinutes(10)));

		String uploadUrl = presignedRequest.url().toString();
		String photoUrl = "https://" + bucket + ".s3." + region + ".amazonaws.com/" + objectKey;

		return new PresignUploadResponse(uploadUrl, photoUrl, objectKey);
	}

	private String normalizeContentType(String contentType) {
		if (contentType == null || contentType.isBlank()) {
			throw new IllegalArgumentException("contentType이 필요합니다.");
		}
		return contentType.trim().toLowerCase(Locale.ROOT);
	}

	private void validateContentType(String contentType) {
		if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
			throw new IllegalArgumentException("허용되지 않은 contentType입니다: " + contentType);
		}
	}

	private String resolveExtension(String filename, String contentType) {
		if (filename != null && filename.contains(".")) {
			String ext = filename.substring(filename.lastIndexOf('.')).toLowerCase(Locale.ROOT);
			if (Set.of(".jpg", ".jpeg", ".png", ".webp").contains(ext)) {
				return ext.equals(".jpeg") ? ".jpg" : ext;
			}
		}
		return EXTENSION_BY_CONTENT_TYPE.get(contentType);
	}
}
