package com.pawpawfind.backend.service;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.pawpawfind.backend.client.KakaoGeocodingResult;
import com.pawpawfind.backend.client.KakaoGeocodingResult.Outcome;
import com.pawpawfind.backend.client.KakaoLocalGeocodingClient;
import com.pawpawfind.backend.config.KakaoLocalProperties;
import com.pawpawfind.backend.entity.GeocodeStatus;
import com.pawpawfind.backend.entity.ShelterLocation;
import com.pawpawfind.backend.repository.ShelterLocationRepository;

@Service
public class ShelterGeocodingService {

	private static final Logger log = LoggerFactory.getLogger(ShelterGeocodingService.class);
	private final ShelterLocationRepository repository;
	private final KakaoLocalGeocodingClient client;
	private final KakaoLocalProperties properties;
	private final AtomicBoolean running = new AtomicBoolean();

	public ShelterGeocodingService(ShelterLocationRepository repository,
			KakaoLocalGeocodingClient client, KakaoLocalProperties properties) {
		this.repository = repository;
		this.client = client;
		this.properties = properties;
	}

	public int processBatch() {
		if (!properties.isEnabled() || !running.compareAndSet(false, true)) return 0;
		try {
			List<ShelterLocation> candidates = repository
					.findByGeocodeStatusInAndAttemptCountLessThanOrderByIdAsc(
							EnumSet.of(GeocodeStatus.PENDING, GeocodeStatus.FAILED),
							properties.getMaxAttempts(), PageRequest.of(0, properties.getBatchSize()));
			Set<String> processedKeys = new HashSet<>();
			int processed = 0;
			for (ShelterLocation location : candidates) {
				if (!processedKeys.add(location.getShelterKey())) continue;
				try {
					processOne(location);
					processed++;
				} catch (RuntimeException exception) {
					log.warn("Shelter geocoding persistence failed for shelterKey={}", location.getShelterKey());
				}
			}
			return processed;
		} finally {
			running.set(false);
		}
	}

	private void processOne(ShelterLocation location) {
		KakaoGeocodingResult result = location.getNormalizedAddress() == null
				? KakaoGeocodingResult.of(Outcome.NOT_FOUND)
				: client.geocode(location.getNormalizedAddress());
		location.setAttemptCount(location.getAttemptCount() + 1);
		if (result.outcome() == Outcome.SUCCESS) {
			location.setLatitude(result.latitude());
			location.setLongitude(result.longitude());
			location.setGeocodeStatus(GeocodeStatus.SUCCESS);
			location.setGeocodeProvider("KAKAO");
			location.setGeocodedAt(LocalDateTime.now());
		} else if (result.outcome() == Outcome.NOT_FOUND) {
			location.setGeocodeStatus(GeocodeStatus.NOT_FOUND);
			location.setGeocodeProvider(null);
			location.setGeocodedAt(null);
		} else {
			location.setGeocodeStatus(GeocodeStatus.FAILED);
			location.setGeocodeProvider(null);
			location.setGeocodedAt(null);
			if (!result.retryable()) location.setAttemptCount(properties.getMaxAttempts());
		}
		repository.save(location);
	}
}
