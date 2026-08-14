package com.pawpawfind.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pawpawfind.backend.entity.Animal;

/** 공공 공고. PK는 desertionNo(String). */
public interface AnimalRepository extends JpaRepository<Animal, String> {
}
