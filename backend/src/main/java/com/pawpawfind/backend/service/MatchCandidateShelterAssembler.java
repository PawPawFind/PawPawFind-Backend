package com.pawpawfind.backend.service;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.pawpawfind.backend.dto.MatchCandidateDto;
import com.pawpawfind.backend.dto.MatchShelterDto;
import com.pawpawfind.backend.entity.Animal;
import com.pawpawfind.backend.entity.GeocodeStatus;
import com.pawpawfind.backend.entity.MatchResult;
import com.pawpawfind.backend.entity.ShelterLocation;
import com.pawpawfind.backend.repository.AnimalRepository;
import com.pawpawfind.backend.repository.ShelterLocationRepository;

/** 반환 대상으로 확정된 매칭 후보에 보호소 정보를 일괄 조립한다. */
@Component
public class MatchCandidateShelterAssembler {

	private final AnimalRepository animalRepository;
	private final ShelterLocationRepository shelterLocationRepository;
	private final ShelterIdentity shelterIdentity;

	public MatchCandidateShelterAssembler(
			AnimalRepository animalRepository,
			ShelterLocationRepository shelterLocationRepository,
			ShelterIdentity shelterIdentity) {
		this.animalRepository = animalRepository;
		this.shelterLocationRepository = shelterLocationRepository;
		this.shelterIdentity = shelterIdentity;
	}

	public void enrich(List<MatchCandidateDto> candidates) {
		Set<String> desertionNos = candidates.stream()
				.filter(this::isShelterCandidate)
				.map(MatchCandidateDto::getDesertionNo)
				.filter(value -> value != null && !value.isBlank())
				.collect(Collectors.toCollection(LinkedHashSet::new));
		if (desertionNos.isEmpty()) return;

		Map<String, Animal> animals = animalRepository.findAllById(desertionNos).stream()
				.collect(Collectors.toMap(Animal::getDesertionNo, Function.identity()));
		Map<String, String> shelterKeysByAnimal = new LinkedHashMap<>();
		Set<String> shelterKeys = new LinkedHashSet<>();
		for (Animal animal : animals.values()) {
			shelterIdentity.resolve(animal.getCareRegNo(), animal.getCareAddr()).ifPresent(identity -> {
				shelterKeysByAnimal.put(animal.getDesertionNo(), identity.shelterKey());
				shelterKeys.add(identity.shelterKey());
			});
		}

		Map<String, ShelterLocation> locations = loadLocations(shelterKeys);
		for (MatchCandidateDto candidate : candidates) {
			if (!isShelterCandidate(candidate)) continue;
			Animal animal = animals.get(candidate.getDesertionNo());
			if (animal == null) continue;
			candidate.setShelter(toShelter(animal,
					locations.get(shelterKeysByAnimal.get(animal.getDesertionNo()))));
		}
	}

	private Map<String, ShelterLocation> loadLocations(Collection<String> shelterKeys) {
		if (shelterKeys.isEmpty()) return Map.of();
		return shelterLocationRepository.findAllByShelterKeyIn(shelterKeys).stream()
				.collect(Collectors.toMap(ShelterLocation::getShelterKey, Function.identity()));
	}

	private boolean isShelterCandidate(MatchCandidateDto candidate) {
		return candidate != null && MatchResult.CANDIDATE_SHELTER.equals(candidate.getCandidateType());
	}

	private MatchShelterDto toShelter(Animal animal, ShelterLocation location) {
		MatchShelterDto shelter = new MatchShelterDto();
		shelter.setCareRegNo(animal.getCareRegNo());
		shelter.setName(animal.getCareNm());
		shelter.setAddress(animal.getCareAddr());
		shelter.setTelephone(animal.getCareTel());
		if (hasValidCoordinates(location)) {
			shelter.setLatitude(location.getLatitude());
			shelter.setLongitude(location.getLongitude());
		}
		return shelter;
	}

	private boolean hasValidCoordinates(ShelterLocation location) {
		if (location == null || location.getGeocodeStatus() != GeocodeStatus.SUCCESS) return false;
		Double latitude = location.getLatitude();
		Double longitude = location.getLongitude();
		return latitude != null && longitude != null
				&& Double.isFinite(latitude) && Double.isFinite(longitude)
				&& latitude >= -90.0 && latitude <= 90.0
				&& longitude >= -180.0 && longitude <= 180.0;
	}
}
