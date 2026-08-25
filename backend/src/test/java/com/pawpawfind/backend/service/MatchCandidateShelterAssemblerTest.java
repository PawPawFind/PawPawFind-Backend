package com.pawpawfind.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import com.pawpawfind.backend.dto.MatchCandidateDto;
import com.pawpawfind.backend.entity.Animal;
import com.pawpawfind.backend.entity.GeocodeStatus;
import com.pawpawfind.backend.entity.ShelterLocation;
import com.pawpawfind.backend.repository.AnimalRepository;
import com.pawpawfind.backend.repository.ShelterLocationRepository;

class MatchCandidateShelterAssemblerTest {

	private AnimalRepository animalRepository;
	private ShelterLocationRepository shelterLocationRepository;
	private MatchCandidateShelterAssembler assembler;

	@BeforeEach
	void setUp() {
		animalRepository = mock(AnimalRepository.class);
		shelterLocationRepository = mock(ShelterLocationRepository.class);
		assembler = new MatchCandidateShelterAssembler(
				animalRepository, shelterLocationRepository, new ShelterIdentity());
	}

	@Test
	void enrichesTextAndSuccessfulCoordinatesUsingRegistrationKey() {
		Animal animal = animal("A-1", " REG-1 ", "행복 보호소", " 서울시  송파구 ", "02-1234");
		ShelterLocation location = location("REG:REG-1", GeocodeStatus.SUCCESS, 37.5, 127.1);
		when(animalRepository.findAllById(any())).thenReturn(List.of(animal));
		when(shelterLocationRepository.findAllByShelterKeyIn(any())).thenReturn(List.of(location));
		MatchCandidateDto candidate = shelterCandidate("A-1");

		assembler.enrich(List.of(candidate));

		assertThat(candidate.getShelter().getCareRegNo()).isEqualTo(" REG-1 ");
		assertThat(candidate.getShelter().getName()).isEqualTo("행복 보호소");
		assertThat(candidate.getShelter().getAddress()).isEqualTo(" 서울시  송파구 ");
		assertThat(candidate.getShelter().getTelephone()).isEqualTo("02-1234");
		assertThat(candidate.getShelter().getLatitude()).isEqualTo(37.5);
		assertThat(candidate.getShelter().getLongitude()).isEqualTo(127.1);
		assertLocationKeysContainExactly("REG:REG-1");
	}

	@Test
	void usesAddressHashFallbackAndKeepsTextWhenLocationIsMissing() {
		Animal animal = animal("A-1", null, "행복 보호소", " 서울시  송파구 ", "02-1234");
		when(animalRepository.findAllById(any())).thenReturn(List.of(animal));
		when(shelterLocationRepository.findAllByShelterKeyIn(any())).thenReturn(List.of());
		MatchCandidateDto candidate = shelterCandidate("A-1");

		assembler.enrich(List.of(candidate));

		assertThat(candidate.getShelter()).isNotNull();
		assertThat(candidate.getShelter().getName()).isEqualTo("행복 보호소");
		assertThat(candidate.getShelter().getLatitude()).isNull();
		assertThat(candidate.getShelter().getLongitude()).isNull();
		String expectedKey = new ShelterIdentity().resolve(null, "서울시 송파구").orElseThrow().shelterKey();
		assertLocationKeysContainExactly(expectedKey);
	}

	@ParameterizedTest
	@EnumSource(value = GeocodeStatus.class, names = { "PENDING", "NOT_FOUND", "FAILED" })
	void omitsCoordinatesUnlessStatusIsSuccess(GeocodeStatus status) {
		assertCoordinatesAreNull(location("REG:R", status, 37.5, 127.1));
	}

	@ParameterizedTest
	@ValueSource(strings = { "LATITUDE_ONLY", "LONGITUDE_ONLY", "NAN_LATITUDE", "INFINITE_LONGITUDE",
			"LATITUDE_OUT_OF_RANGE", "LONGITUDE_OUT_OF_RANGE" })
	void omitsBothCoordinatesWhenEitherCoordinateIsInvalid(String caseName) {
		ShelterLocation location = location("REG:R", GeocodeStatus.SUCCESS, 37.5, 127.1);
		switch (caseName) {
			case "LATITUDE_ONLY" -> location.setLongitude(null);
			case "LONGITUDE_ONLY" -> location.setLatitude(null);
			case "NAN_LATITUDE" -> location.setLatitude(Double.NaN);
			case "INFINITE_LONGITUDE" -> location.setLongitude(Double.POSITIVE_INFINITY);
			case "LATITUDE_OUT_OF_RANGE" -> location.setLatitude(90.1);
			case "LONGITUDE_OUT_OF_RANGE" -> location.setLongitude(-180.1);
			default -> throw new IllegalArgumentException(caseName);
		}
		assertCoordinatesAreNull(location);
	}

	@Test
	void keepsReportAndMissingAnimalCandidatesWithoutShelter() {
		when(animalRepository.findAllById(any())).thenReturn(List.of());
		MatchCandidateDto missingAnimal = shelterCandidate("missing");
		MatchCandidateDto report = new MatchCandidateDto();
		report.setCandidateType("REPORT");

		assembler.enrich(List.of(missingAnimal, report));

		assertThat(missingAnimal.getShelter()).isNull();
		assertThat(report.getShelter()).isNull();
		verify(shelterLocationRepository, never()).findAllByShelterKeyIn(any());
	}

	@Test
	void deduplicatesBatchLookupsAndPreservesCandidateOrder() {
		Animal first = animal("A-1", "R", "첫째", "주소", "1");
		Animal second = animal("A-2", "R", "둘째", "주소", "2");
		when(animalRepository.findAllById(any())).thenReturn(List.of(first, second));
		when(shelterLocationRepository.findAllByShelterKeyIn(any()))
				.thenReturn(List.of(location("REG:R", GeocodeStatus.SUCCESS, 37.0, 127.0)));
		MatchCandidateDto firstCandidate = shelterCandidate("A-1");
		MatchCandidateDto duplicate = shelterCandidate("A-1");
		MatchCandidateDto secondCandidate = shelterCandidate("A-2");
		List<MatchCandidateDto> candidates = List.of(firstCandidate, duplicate, secondCandidate);

		assembler.enrich(candidates);

		assertThat(candidates).containsExactly(firstCandidate, duplicate, secondCandidate);
		assertThat(candidates).extracting(candidate -> candidate.getShelter().getName())
				.containsExactly("첫째", "첫째", "둘째");
		verify(animalRepository, times(1)).findAllById(any());
		verify(shelterLocationRepository, times(1)).findAllByShelterKeyIn(any());
		ArgumentCaptor<Iterable<String>> animals = iterableCaptor();
		verify(animalRepository).findAllById(animals.capture());
		assertThat(animals.getValue()).containsExactly("A-1", "A-2");
		assertLocationKeysContainExactly("REG:R");
		verify(animalRepository, never()).existsById(any());
	}

	private void assertCoordinatesAreNull(ShelterLocation location) {
		Animal animal = animal("A-1", "R", "보호소", "주소", "전화");
		when(animalRepository.findAllById(any())).thenReturn(List.of(animal));
		when(shelterLocationRepository.findAllByShelterKeyIn(any())).thenReturn(List.of(location));
		MatchCandidateDto candidate = shelterCandidate("A-1");
		assembler.enrich(List.of(candidate));
		assertThat(candidate.getShelter().getLatitude()).isNull();
		assertThat(candidate.getShelter().getLongitude()).isNull();
	}

	@SuppressWarnings("unchecked")
	private ArgumentCaptor<Iterable<String>> iterableCaptor() {
		return ArgumentCaptor.forClass(Iterable.class);
	}

	@SuppressWarnings("unchecked")
	private void assertLocationKeysContainExactly(String... keys) {
		ArgumentCaptor<Collection<String>> captor = ArgumentCaptor.forClass(Collection.class);
		verify(shelterLocationRepository).findAllByShelterKeyIn(captor.capture());
		assertThat(captor.getValue()).containsExactly(keys);
	}

	private MatchCandidateDto shelterCandidate(String desertionNo) {
		MatchCandidateDto candidate = new MatchCandidateDto();
		candidate.setCandidateType("SHELTER");
		candidate.setDesertionNo(desertionNo);
		return candidate;
	}

	private Animal animal(String desertionNo, String careRegNo, String name, String address, String telephone) {
		Animal animal = new Animal();
		animal.setDesertionNo(desertionNo);
		animal.setCareRegNo(careRegNo);
		animal.setCareNm(name);
		animal.setCareAddr(address);
		animal.setCareTel(telephone);
		return animal;
	}

	private ShelterLocation location(String key, GeocodeStatus status, Double latitude, Double longitude) {
		ShelterLocation location = new ShelterLocation();
		location.setShelterKey(key);
		location.setGeocodeStatus(status);
		location.setLatitude(latitude);
		location.setLongitude(longitude);
		return location;
	}
}
