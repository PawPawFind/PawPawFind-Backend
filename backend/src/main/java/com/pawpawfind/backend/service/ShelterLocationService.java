package com.pawpawfind.backend.service;

import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.pawpawfind.backend.entity.Animal;
import com.pawpawfind.backend.entity.GeocodeStatus;
import com.pawpawfind.backend.entity.ShelterLocation;
import com.pawpawfind.backend.repository.ShelterLocationRepository;
import com.pawpawfind.backend.service.ShelterIdentity.Values;

@Service
public class ShelterLocationService {

	private static final Logger log = LoggerFactory.getLogger(ShelterLocationService.class);
	private final ShelterLocationRepository repository;
	private final ShelterIdentity identity;

	public ShelterLocationService(ShelterLocationRepository repository, ShelterIdentity identity) {
		this.repository = repository;
		this.identity = identity;
	}

	public Optional<ShelterLocation> upsert(Animal animal) {
		if (animal == null) return Optional.empty();
		Optional<Values> resolved = identity.resolve(animal.getCareRegNo(), animal.getCareAddr());
		if (resolved.isEmpty()) return Optional.empty();
		Values values = resolved.get();
		try {
			ShelterLocation location = repository.findByShelterKey(values.shelterKey())
					.orElseGet(() -> newLocation(values));
			applyInformation(location, animal, values);
			return Optional.of(repository.save(location));
		} catch (DataIntegrityViolationException exception) {
			log.debug("Concurrent shelter location upsert detected for shelterKey={}", values.shelterKey());
			return repository.findByShelterKey(values.shelterKey());
		}
	}

	private ShelterLocation newLocation(Values values) {
		ShelterLocation location = new ShelterLocation();
		location.setShelterKey(values.shelterKey());
		location.setGeocodeStatus(GeocodeStatus.PENDING);
		return location;
	}

	private void applyInformation(ShelterLocation location, Animal animal, Values values) {
		boolean addressChanged = location.getId() != null
				&& !Objects.equals(location.getAddressHash(), values.addressHash());
		location.setCareRegNo(values.careRegNo());
		location.setCareNm(trimToNull(animal.getCareNm()));
		location.setCareTel(trimToNull(animal.getCareTel()));
		location.setCareAddr(values.careAddr());
		location.setNormalizedAddress(values.normalizedAddress());
		location.setAddressHash(values.addressHash());
		if (addressChanged) {
			location.setLatitude(null);
			location.setLongitude(null);
			location.setGeocodeStatus(GeocodeStatus.PENDING);
			location.setGeocodeProvider(null);
			location.setAttemptCount(0);
			location.setGeocodedAt(null);
		}
	}

	private String trimToNull(String value) {
		if (value == null) return null;
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
