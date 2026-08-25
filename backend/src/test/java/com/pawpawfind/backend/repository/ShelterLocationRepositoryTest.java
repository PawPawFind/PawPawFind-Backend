package com.pawpawfind.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumSet;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.pawpawfind.backend.entity.GeocodeStatus;
import com.pawpawfind.backend.entity.ShelterLocation;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ShelterLocationRepositoryTest {

	@Autowired
	private ShelterLocationRepository repository;

	@Test
	void selectsOnlyPendingAndRetryableFailedWithinBatchSize() {
		ShelterLocation pending = repository.save(location("REG:1", GeocodeStatus.PENDING, 0));
		ShelterLocation failed = repository.save(location("REG:2", GeocodeStatus.FAILED, 2));
		repository.save(location("REG:3", GeocodeStatus.SUCCESS, 1));
		repository.save(location("REG:4", GeocodeStatus.NOT_FOUND, 1));
		repository.save(location("REG:5", GeocodeStatus.FAILED, 3));
		repository.save(location("REG:6", GeocodeStatus.PENDING, 0));

		List<ShelterLocation> candidates = repository
				.findByGeocodeStatusInAndAttemptCountLessThanOrderByIdAsc(
						EnumSet.of(GeocodeStatus.PENDING, GeocodeStatus.FAILED),
						3, PageRequest.of(0, 2));

		assertThat(candidates).extracting(ShelterLocation::getId)
				.containsExactly(pending.getId(), failed.getId());
	}

	private ShelterLocation location(String key, GeocodeStatus status, int attempts) {
		ShelterLocation location = new ShelterLocation();
		location.setShelterKey(key);
		location.setNormalizedAddress("서울시 중구");
		location.setAddressHash("hash-" + key);
		location.setGeocodeStatus(status);
		location.setAttemptCount(attempts);
		return location;
	}
}
