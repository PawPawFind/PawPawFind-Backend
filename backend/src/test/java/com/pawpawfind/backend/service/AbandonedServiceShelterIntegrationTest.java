package com.pawpawfind.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.pawpawfind.backend.entity.Animal;
import com.pawpawfind.backend.repository.AnimalRepository;

class AbandonedServiceShelterIntegrationTest {

	private AnimalRepository animalRepository;
	private AnimalEmbedTriggerService embedTriggerService;
	private ShelterLocationService locationService;
	private ShelterGeocodingService geocodingService;
	private MockRestServiceServer server;
	private AbandonedService service;

	@BeforeEach
	void setUp() {
		animalRepository = mock(AnimalRepository.class);
		embedTriggerService = mock(AnimalEmbedTriggerService.class);
		locationService = mock(ShelterLocationService.class);
		geocodingService = mock(ShelterGeocodingService.class);
		RestClient.Builder builder = RestClient.builder();
		server = MockRestServiceServer.bindTo(builder).build();
		service = new AbandonedService(animalRepository, embedTriggerService, locationService,
				geocodingService, new ShelterIdentity(), builder.build());
		ReflectionTestUtils.setField(service, "apiKey", "test-key");
		ReflectionTestUtils.setField(service, "apiUrl", "http://public.test/animals");
		when(animalRepository.save(any(Animal.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(animalRepository.findAll()).thenReturn(List.of());
	}

	@Test
	void deduplicatesSheltersAndPreservesAnimalSaveAndEmbeddingFlow() {
		respond(twoAnimals("REG-1", "REG-1"));

		Object result = service.syncAbandonedAnimals();

		assertThat(result).isEqualTo(List.of());
		verify(animalRepository, times(2)).save(any(Animal.class));
		verify(embedTriggerService, times(2)).triggerIfMissing(any(Animal.class));
		verify(locationService, times(1)).upsert(any(Animal.class));
		verify(geocodingService).processBatch();
		server.verify();
	}

	@Test
	void shelterFailureDoesNotStopAnimalSaveEmbeddingOrFollowingShelter() {
		respond(twoAnimals("REG-1", "REG-2"));
		doThrow(new IllegalStateException("location failure")).doReturn(java.util.Optional.empty())
				.when(locationService).upsert(any(Animal.class));

		service.syncAbandonedAnimals();

		verify(animalRepository, times(2)).save(any(Animal.class));
		verify(embedTriggerService, times(2)).triggerIfMissing(any(Animal.class));
		verify(locationService, times(2)).upsert(any(Animal.class));
		verify(geocodingService).processBatch();
	}

	@Test
	void geocodingBatchFailureDoesNotRollBackAnimalSynchronization() {
		respond(twoAnimals("REG-1", "REG-1"));
		doThrow(new IllegalStateException("geocoding failure")).when(geocodingService).processBatch();

		service.syncAbandonedAnimals();

		verify(animalRepository, times(2)).save(any(Animal.class));
		verify(embedTriggerService, times(2)).triggerIfMissing(any(Animal.class));
	}

	private void respond(String body) {
		server.expect(requestTo(org.hamcrest.Matchers.startsWith("http://public.test/animals?")))
				.andRespond(withSuccess(body, MediaType.APPLICATION_JSON));
	}

	private String twoAnimals(String firstRegNo, String secondRegNo) {
		return """
				{"response":{"body":{"totalCount":2,"items":{"item":[
				{"desertionNo":"A1","careRegNo":"%s","careNm":"보호소","careTel":"02-1","careAddr":"서울시 중구"},
				{"desertionNo":"A2","careRegNo":"%s","careNm":"보호소","careTel":"02-1","careAddr":"서울시 중구"}
				]}}}}
				""".formatted(firstRegNo, secondRegNo);
	}
}
