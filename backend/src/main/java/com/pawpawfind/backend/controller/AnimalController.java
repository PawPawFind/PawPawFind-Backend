package com.pawpawfind.backend.controller;


import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import com.pawpawfind.backend.entity.Animal;
import com.pawpawfind.backend.repository.AnimalRepository;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

@RestController 

public class AnimalController {
    private final AnimalRepository animalRepository;

    public AnimalController(AnimalRepository animalRepository){
        this.animalRepository = animalRepository;
    
    }

    @GetMapping("/api/animals")
    public List<Animal> getAnimals(){
        return animalRepository.findAll();
    }


    @GetMapping("/api/animals/{desertionNo}")
    public ResponseEntity<Animal> getAnimal(@PathVariable String desertionNo){
        Animal animal = animalRepository.findById(desertionNo).orElse(null);

        if (animal == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(animal);
    }
}