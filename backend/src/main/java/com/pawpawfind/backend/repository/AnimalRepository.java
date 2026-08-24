package com.pawpawfind.backend.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.pawpawfind.backend.entity.Animal;

/** 공공 공고. PK는 desertionNo(String). */
public interface AnimalRepository extends JpaRepository<Animal, String> {

	List<Animal> findByUpdatedAtAfterOrderByUpdatedAtAsc(LocalDateTime since);

	@Query("""
			SELECT a FROM Animal a
			WHERE (
				a.popfile1 IS NOT NULL AND TRIM(a.popfile1) <> ''
				OR a.popfile2 IS NOT NULL AND TRIM(a.popfile2) <> ''
			)
			AND NOT EXISTS (
				SELECT 1 FROM AnimalEmbedding e WHERE e.desertionNo = a.desertionNo
			)
			ORDER BY a.updatedAt ASC
			""")
	List<Animal> findAnimalsMissingEmbeddings();
}
