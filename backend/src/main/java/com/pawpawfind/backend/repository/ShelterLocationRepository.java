package com.pawpawfind.backend.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.pawpawfind.backend.entity.GeocodeStatus;
import com.pawpawfind.backend.entity.ShelterLocation;

public interface ShelterLocationRepository extends JpaRepository<ShelterLocation, Long> {

	Optional<ShelterLocation> findByShelterKey(String shelterKey);

	List<ShelterLocation> findAllByShelterKeyIn(Collection<String> shelterKeys);

	List<ShelterLocation> findByGeocodeStatusInAndAttemptCountLessThanOrderByIdAsc(
			Collection<GeocodeStatus> statuses, int maxAttempts, Pageable pageable);
}
