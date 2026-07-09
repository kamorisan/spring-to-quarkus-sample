/*
 * Copyright 2012-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.api;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.api.dto.VisitDto;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.Visit;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * REST API controller for Visit resources.
 */
@RestController
@RequestMapping("/api/owners/{ownerId}/pets/{petId}/visits")
@CrossOrigin(origins = "http://localhost:3000")
public class VisitApiController {

	private final OwnerRepository ownerRepository;

	public VisitApiController(OwnerRepository ownerRepository) {
		this.ownerRepository = ownerRepository;
	}

	/**
	 * Get all visits for a pet.
	 * @param ownerId owner ID
	 * @param petId pet ID
	 * @return list of visits
	 */
	@GetMapping
	public ResponseEntity<List<VisitDto>> getVisits(@PathVariable Integer ownerId, @PathVariable Integer petId) {
		Owner owner = ownerRepository.findById(ownerId)
			.orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId));

		Pet pet = owner.getPet(petId);
		if (pet == null) {
			throw new IllegalArgumentException("Pet not found with id: " + petId);
		}

		List<VisitDto> visits = pet.getVisits().stream().map(VisitDto::fromEntity).collect(Collectors.toList());

		return ResponseEntity.ok(visits);
	}

	/**
	 * Create a new visit for a pet.
	 * @param ownerId owner ID
	 * @param petId pet ID
	 * @param visitDto visit data
	 * @return created visit
	 */
	@PostMapping
	public ResponseEntity<VisitDto> createVisit(@PathVariable Integer ownerId, @PathVariable Integer petId,
			@Valid @RequestBody VisitDto visitDto) {
		Owner owner = ownerRepository.findById(ownerId)
			.orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId));

		Pet pet = owner.getPet(petId);
		if (pet == null) {
			throw new IllegalArgumentException("Pet not found with id: " + petId);
		}

		// Validate visit date is in the future
		if (visitDto.getDate() != null && !visitDto.getDate().isAfter(LocalDate.now())) {
			throw new IllegalArgumentException("Visit date must be after today");
		}

		// Create visit
		Visit visit = visitDto.toEntity();
		pet.addVisit(visit);
		ownerRepository.save(owner);

		return ResponseEntity.status(HttpStatus.CREATED).body(VisitDto.fromEntity(visit));
	}

}
