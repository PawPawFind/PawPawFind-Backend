package com.pawpawfind.backend.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;

import org.springframework.stereotype.Component;

@Component
public class ShelterIdentity {

	public Optional<Values> resolve(String careRegNo, String careAddr) {
		String normalizedRegNo = trimToNull(careRegNo);
		String originalAddress = trimToNull(careAddr);
		String normalizedAddress = normalizeAddress(originalAddress);
		String addressHash = normalizedAddress == null ? null : sha256(normalizedAddress);
		if (normalizedRegNo != null) {
			return Optional.of(new Values("REG:" + normalizedRegNo, normalizedRegNo,
					originalAddress, normalizedAddress, addressHash));
		}
		if (normalizedAddress != null) {
			return Optional.of(new Values("ADDR:" + addressHash, null,
					originalAddress, normalizedAddress, addressHash));
		}
		return Optional.empty();
	}

	public String normalizeAddress(String address) {
		String trimmed = trimToNull(address);
		return trimmed == null ? null : trimmed.replaceAll("\\s+", " ");
	}

	private String trimToNull(String value) {
		if (value == null) return null;
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private String sha256(String value) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256")
					.digest(value.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 is not available", exception);
		}
	}

	public record Values(String shelterKey, String careRegNo, String careAddr,
			String normalizedAddress, String addressHash) {
	}
}
