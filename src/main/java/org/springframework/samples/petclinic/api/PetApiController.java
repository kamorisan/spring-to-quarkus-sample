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

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.api.dto.PetDto;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetType;
import org.springframework.samples.petclinic.owner.PetTypeRepository;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * REST API controller for Pet resources.
 */
@RestController
@RequestMapping("/api/owners/{ownerId}/pets")
@CrossOrigin(origins = "http://localhost:3000")
public class PetApiController {

	private final OwnerRepository ownerRepository;

	private final PetTypeRepository petTypeRepository;

	public PetApiController(OwnerRepository ownerRepository, PetTypeRepository petTypeRepository) {
		this.ownerRepository = ownerRepository;
		this.petTypeRepository = petTypeRepository;
	}

	/**
	 * Get pet by ID.
	 * @param ownerId owner ID
	 * @param petId pet ID
	 * @return pet details
	 */
	@GetMapping("/{petId}")
	public ResponseEntity<PetDto> getPet(@PathVariable Integer ownerId, @PathVariable Integer petId) {
		Owner owner = ownerRepository.findById(ownerId)
			.orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId));

		Pet pet = owner.getPet(petId);
		if (pet == null) {
			throw new IllegalArgumentException("Pet not found with id: " + petId);
		}

		return ResponseEntity.ok(PetDto.fromEntity(pet));
	}

	/**
	 * Add a new pet to an owner.
	 * @param ownerId owner ID
	 * @param petDto pet data
	 * @return created pet
	 */
	@PostMapping
	public ResponseEntity<PetDto> createPet(@PathVariable Integer ownerId, @Valid @RequestBody PetDto petDto) {
		Owner owner = ownerRepository.findById(ownerId)
			.orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId));

		// Validate pet data
		validatePet(petDto, owner, null);

		// Create pet
		Pet pet = new Pet();
		pet.setName(petDto.getName());
		pet.setBirthDate(petDto.getBirthDate());

		// Set pet type - use petTypeId if provided, otherwise fall back to type.id
		Integer petTypeId = petDto.getPetTypeId() != null ? petDto.getPetTypeId()
				: (petDto.getType() != null ? petDto.getType().getId() : null);
		if (petTypeId == null) {
			throw new IllegalArgumentException("Pet type is required");
		}
		PetType petType = petTypeRepository.findById(petTypeId)
			.orElseThrow(() -> new IllegalArgumentException("Pet type not found with id: " + petTypeId));
		pet.setType(petType);

		// Add pet to owner
		owner.addPet(pet);
		ownerRepository.save(owner);

		return ResponseEntity.status(HttpStatus.CREATED).body(PetDto.fromEntity(pet));
	}

	/**
	 * Update an existing pet.
	 * @param ownerId owner ID
	 * @param petId pet ID
	 * @param petDto updated pet data
	 * @return updated pet
	 */
	@PutMapping("/{petId}")
	public ResponseEntity<PetDto> updatePet(@PathVariable Integer ownerId, @PathVariable Integer petId,
			@Valid @RequestBody PetDto petDto) {
		Owner owner = ownerRepository.findById(ownerId)
			.orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId));

		Pet pet = owner.getPet(petId);
		if (pet == null) {
			throw new IllegalArgumentException("Pet not found with id: " + petId);
		}

		// Validate pet data
		validatePet(petDto, owner, petId);

		// Update pet
		pet.setName(petDto.getName());
		pet.setBirthDate(petDto.getBirthDate());

		// Update pet type - use petTypeId if provided, otherwise fall back to type.id
		Integer petTypeId = petDto.getPetTypeId() != null ? petDto.getPetTypeId()
				: (petDto.getType() != null ? petDto.getType().getId() : null);
		if (petTypeId == null) {
			throw new IllegalArgumentException("Pet type is required");
		}
		PetType petType = petTypeRepository.findById(petTypeId)
			.orElseThrow(() -> new IllegalArgumentException("Pet type not found with id: " + petTypeId));
		pet.setType(petType);

		ownerRepository.save(owner);

		return ResponseEntity.ok(PetDto.fromEntity(pet));
	}

	/**
	 * Delete a pet.
	 * @param ownerId owner ID
	 * @param petId pet ID
	 * @return no content
	 */
	@DeleteMapping("/{petId}")
	public ResponseEntity<Void> deletePet(@PathVariable Integer ownerId, @PathVariable Integer petId) {
		Owner owner = ownerRepository.findById(ownerId)
			.orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId));

		Pet pet = owner.getPet(petId);
		if (pet == null) {
			throw new IllegalArgumentException("Pet not found with id: " + petId);
		}

		owner.getPets().remove(pet);
		ownerRepository.save(owner);

		return ResponseEntity.noContent().build();
	}

	/**
	 * Validate pet data (similar to PetValidator logic).
	 * @param petDto pet data
	 * @param owner owner
	 * @param existingPetId existing pet ID (null for new pets)
	 */
	private void validatePet(PetDto petDto, Owner owner, Integer existingPetId) {
		// Name validation
		if (!StringUtils.hasText(petDto.getName())) {
			throw new IllegalArgumentException("Pet name is required");
		}

		// Birth date validation - cannot be in the future
		if (petDto.getBirthDate() != null && petDto.getBirthDate().isAfter(LocalDate.now())) {
			throw new IllegalArgumentException("Birth date cannot be in the future");
		}

		// Duplicate name validation (excluding self for updates)
		Pet existingPet = owner.getPet(petDto.getName());
		if (existingPet != null && !existingPet.getId().equals(existingPetId)) {
			throw new IllegalArgumentException("Pet name must be unique for this owner");
		}
	}

}
