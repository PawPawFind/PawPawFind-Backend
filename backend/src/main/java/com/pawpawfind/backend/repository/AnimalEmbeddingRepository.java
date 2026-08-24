package com.pawpawfind.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pawpawfind.backend.entity.AnimalEmbedding;

public interface AnimalEmbeddingRepository extends JpaRepository<AnimalEmbedding, Long> {

	List<AnimalEmbedding> findBySpeciesAndModelVersionAndPreprocessVersion(
			String species, String modelVersion, String preprocessVersion);

	AnimalEmbedding findByGalleryId(String galleryId);

	boolean existsByDesertionNo(String desertionNo);
}
