package com.pawpawfind.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.pawpawfind.backend.entity.Animal;
import com.pawpawfind.backend.entity.GeocodeStatus;
import com.pawpawfind.backend.entity.ShelterLocation;
import com.pawpawfind.backend.repository.ShelterLocationRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ShelterLocationServiceTest {

	@Autowired
	private ShelterLocationService service;

	@Autowired
	private ShelterLocationRepository repository;

	@Test
	void createsPendingLocationAndPreventsDuplicateShelterKey() {
		ShelterLocation created = service.upsert(animal("REG-1", "보호소", "02-1", "서울시 중구"))
				.orElseThrow();
		service.upsert(animal("REG-1", "보호소", "02-1", "서울시 중구"));

		assertThat(created.getShelterKey()).isEqualTo("REG:REG-1");
		assertThat(created.getGeocodeStatus()).isEqualTo(GeocodeStatus.PENDING);
		assertThat(created.getAttemptCount()).isZero();
		assertThat(repository.count()).isEqualTo(1);
	}

	@Test
	void nameAndTelephoneChangesPreserveSuccessfulCoordinatesForSameAddress() {
		ShelterLocation location = service.upsert(animal("REG-2", "이전 이름", "02-1", "서울시 중구"))
				.orElseThrow();
		markSuccess(location);

		ShelterLocation updated = service.upsert(animal("REG-2", "새 이름", "02-2", " 서울시  중구 "))
				.orElseThrow();

		assertThat(updated.getCareNm()).isEqualTo("새 이름");
		assertThat(updated.getCareTel()).isEqualTo("02-2");
		assertThat(updated.getLatitude()).isEqualTo(37.5);
		assertThat(updated.getLongitude()).isEqualTo(127.0);
		assertThat(updated.getGeocodeStatus()).isEqualTo(GeocodeStatus.SUCCESS);
		assertThat(updated.getAttemptCount()).isEqualTo(1);
	}

	@Test
	void addressChangeResetsGeocodingState() {
		ShelterLocation location = service.upsert(animal("REG-3", "보호소", "02-1", "서울시 중구"))
				.orElseThrow();
		markSuccess(location);

		ShelterLocation updated = service.upsert(animal("REG-3", "보호소", "02-1", "서울시 종로구"))
				.orElseThrow();

		assertThat(updated.getLatitude()).isNull();
		assertThat(updated.getLongitude()).isNull();
		assertThat(updated.getGeocodeStatus()).isEqualTo(GeocodeStatus.PENDING);
		assertThat(updated.getAttemptCount()).isZero();
		assertThat(updated.getGeocodedAt()).isNull();
		assertThat(updated.getGeocodeProvider()).isNull();
	}

	@Test
	void repeatedAnimalSaveDoesNotOverwriteSuccessfulCoordinates() {
		Animal animal = animal("REG-4", "보호소", "02-1", "서울시 중구");
		ShelterLocation location = service.upsert(animal).orElseThrow();
		markSuccess(location);

		ShelterLocation updated = service.upsert(animal).orElseThrow();

		assertThat(updated.getLatitude()).isEqualTo(37.5);
		assertThat(updated.getGeocodeStatus()).isEqualTo(GeocodeStatus.SUCCESS);
	}

	@Test
	void missingIdentityIsSkippedAndShelterKeyIsRequiredAndUnique() {
		assertThat(service.upsert(animal(null, "보호소", "02-1", null))).isEmpty();

		ShelterLocation invalid = new ShelterLocation();
		assertThatThrownBy(() -> repository.saveAndFlush(invalid))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	private void markSuccess(ShelterLocation location) {
		location.setLatitude(37.5);
		location.setLongitude(127.0);
		location.setGeocodeStatus(GeocodeStatus.SUCCESS);
		location.setGeocodeProvider("KAKAO");
		location.setAttemptCount(1);
		location.setGeocodedAt(LocalDateTime.now());
		repository.saveAndFlush(location);
	}

	private Animal animal(String regNo, String name, String tel, String address) {
		Animal animal = new Animal();
		animal.setCareRegNo(regNo);
		animal.setCareNm(name);
		animal.setCareTel(tel);
		animal.setCareAddr(address);
		return animal;
	}
}
