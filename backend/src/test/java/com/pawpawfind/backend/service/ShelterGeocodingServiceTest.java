package com.pawpawfind.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import com.pawpawfind.backend.client.KakaoGeocodingResult;
import com.pawpawfind.backend.client.KakaoGeocodingResult.Outcome;
import com.pawpawfind.backend.client.KakaoLocalGeocodingClient;
import com.pawpawfind.backend.config.KakaoLocalProperties;
import com.pawpawfind.backend.entity.GeocodeStatus;
import com.pawpawfind.backend.entity.ShelterLocation;
import com.pawpawfind.backend.repository.ShelterLocationRepository;

class ShelterGeocodingServiceTest {

	private ShelterLocationRepository repository;
	private KakaoLocalGeocodingClient client;
	private KakaoLocalProperties properties;
	private ShelterGeocodingService service;

	@BeforeEach
	void setUp() {
		repository = mock(ShelterLocationRepository.class);
		client = mock(KakaoLocalGeocodingClient.class);
		properties = new KakaoLocalProperties();
		properties.setEnabled(true);
		properties.setBatchSize(2);
		properties.setMaxAttempts(3);
		properties.afterPropertiesSet();
		service = new ShelterGeocodingService(repository, client, properties);
	}

	@Test
	void disabledConfigurationMakesNoQueryOrNetworkCall() {
		properties.setEnabled(false);
		assertThat(service.processBatch()).isZero();
		verify(repository, never()).findByGeocodeStatusInAndAttemptCountLessThanOrderByIdAsc(
				any(), anyInt(), any(Pageable.class));
		verify(client, never()).geocode(any());
	}

	@Test
	void processesPendingAndRetryableFailedWithinBatchLimit() {
		ShelterLocation pending = location(1L, "REG:1", GeocodeStatus.PENDING, 0);
		ShelterLocation failed = location(2L, "REG:2", GeocodeStatus.FAILED, 1);
		when(repository.findByGeocodeStatusInAndAttemptCountLessThanOrderByIdAsc(
				any(), anyInt(), any(Pageable.class))).thenReturn(List.of(pending, failed));
		when(client.geocode("주소")).thenReturn(KakaoGeocodingResult.success(37.5, 127.0));

		assertThat(service.processBatch()).isEqualTo(2);
		assertThat(pending.getGeocodeStatus()).isEqualTo(GeocodeStatus.SUCCESS);
		assertThat(failed.getGeocodeStatus()).isEqualTo(GeocodeStatus.SUCCESS);
		assertThat(pending.getGeocodeProvider()).isEqualTo("KAKAO");
		verify(client, times(2)).geocode("주소");
		verify(repository).findByGeocodeStatusInAndAttemptCountLessThanOrderByIdAsc(
				any(), anyInt(), any(Pageable.class));
	}

	@Test
	void notFoundIsTerminalAndFailuresRespectRetryability() {
		ShelterLocation notFound = location(1L, "REG:1", GeocodeStatus.PENDING, 0);
		ShelterLocation retryable = location(2L, "REG:2", GeocodeStatus.PENDING, 0);
		when(repository.findByGeocodeStatusInAndAttemptCountLessThanOrderByIdAsc(
				any(), anyInt(), any(Pageable.class))).thenReturn(List.of(notFound, retryable));
		when(client.geocode("주소")).thenReturn(KakaoGeocodingResult.of(Outcome.NOT_FOUND),
				KakaoGeocodingResult.of(Outcome.TIMEOUT_FAILURE));

		service.processBatch();

		assertThat(notFound.getGeocodeStatus()).isEqualTo(GeocodeStatus.NOT_FOUND);
		assertThat(retryable.getGeocodeStatus()).isEqualTo(GeocodeStatus.FAILED);
		assertThat(retryable.getAttemptCount()).isEqualTo(1);
	}

	@Test
	void nonRetryableFailureConsumesMaxAttemptsAndDuplicateKeyIsCalledOnce() {
		ShelterLocation first = location(1L, "REG:1", GeocodeStatus.PENDING, 0);
		ShelterLocation duplicate = location(2L, "REG:1", GeocodeStatus.FAILED, 1);
		when(repository.findByGeocodeStatusInAndAttemptCountLessThanOrderByIdAsc(
				any(), anyInt(), any(Pageable.class))).thenReturn(List.of(first, duplicate));
		when(client.geocode("주소")).thenReturn(KakaoGeocodingResult.of(Outcome.NON_RETRYABLE_FAILURE));

		assertThat(service.processBatch()).isEqualTo(1);
		assertThat(first.getAttemptCount()).isEqualTo(3);
		verify(client, times(1)).geocode("주소");
	}

	@Test
	void onePersistenceFailureDoesNotStopNextShelter() {
		ShelterLocation first = location(1L, "REG:1", GeocodeStatus.PENDING, 0);
		ShelterLocation second = location(2L, "REG:2", GeocodeStatus.PENDING, 0);
		when(repository.findByGeocodeStatusInAndAttemptCountLessThanOrderByIdAsc(
				any(), anyInt(), any(Pageable.class))).thenReturn(List.of(first, second));
		when(client.geocode("주소")).thenReturn(KakaoGeocodingResult.success(37.5, 127.0));
		when(repository.save(first)).thenThrow(new IllegalStateException("db failure"));

		assertThat(service.processBatch()).isEqualTo(1);
		verify(client, times(2)).geocode("주소");
		verify(repository).save(second);
	}

	private ShelterLocation location(Long id, String key, GeocodeStatus status, int attempts) {
		ShelterLocation location = new ShelterLocation();
		location.setId(id);
		location.setShelterKey(key);
		location.setNormalizedAddress("주소");
		location.setGeocodeStatus(status);
		location.setAttemptCount(attempts);
		return location;
	}
}
