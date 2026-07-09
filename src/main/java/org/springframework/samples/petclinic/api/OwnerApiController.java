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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.api.dto.OwnerDto;
import org.springframework.samples.petclinic.api.dto.PagedResponse;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * REST API controller for Owner resources.
 */
@RestController
@RequestMapping("/api/owners")
@CrossOrigin(origins = "http://localhost:3000")
public class OwnerApiController {

	private final OwnerRepository ownerRepository;

	private static final int PAGE_SIZE = 5;

	public OwnerApiController(OwnerRepository ownerRepository) {
		this.ownerRepository = ownerRepository;
	}

	/**
	 * Search owners by last name with pagination.
	 * @param lastName last name to search (empty for all owners)
	 * @param page page number (1-based)
	 * @return paginated response of owners
	 */
	@GetMapping
	public PagedResponse<OwnerDto> searchOwners(@RequestParam(defaultValue = "") String lastName,
			@RequestParam(defaultValue = "1") int page) {
		// Convert 1-based page to 0-based for Spring Data
		PageRequest pageRequest = PageRequest.of(page - 1, PAGE_SIZE);
		Page<OwnerDto> ownerPage = ownerRepository.findByLastNameStartingWith(lastName, pageRequest)
			.map(OwnerDto::fromEntity);

		return new PagedResponse<>(ownerPage.getContent(), page, ownerPage.getTotalPages(),
				ownerPage.getTotalElements());
	}

	/**
	 * Get owner by ID.
	 * @param ownerId owner ID
	 * @return owner details with pets and visits
	 */
	@GetMapping("/{ownerId}")
	public ResponseEntity<OwnerDto> getOwner(@PathVariable Integer ownerId) {
		Owner owner = ownerRepository.findById(ownerId)
			.orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId));
		return ResponseEntity.ok(OwnerDto.fromEntity(owner));
	}

	/**
	 * Create a new owner.
	 * @param ownerDto owner data
	 * @return created owner
	 */
	@PostMapping
	public ResponseEntity<OwnerDto> createOwner(@Valid @RequestBody OwnerDto ownerDto) {
		Owner owner = ownerDto.toEntity();
		Owner savedOwner = ownerRepository.save(owner);
		return ResponseEntity.status(HttpStatus.CREATED).body(OwnerDto.fromEntity(savedOwner));
	}

	/**
	 * Update an existing owner.
	 * @param ownerId owner ID
	 * @param ownerDto updated owner data
	 * @return updated owner
	 */
	@PutMapping("/{ownerId}")
	public ResponseEntity<OwnerDto> updateOwner(@PathVariable Integer ownerId, @Valid @RequestBody OwnerDto ownerDto) {
		// Verify owner exists
		ownerRepository.findById(ownerId)
			.orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId));

		// Prevent ID mismatch
		if (ownerDto.getId() != null && !ownerDto.getId().equals(ownerId)) {
			throw new IllegalArgumentException("Owner ID in path and body must match");
		}

		Owner owner = ownerDto.toEntity();
		owner.setId(ownerId);
		Owner savedOwner = ownerRepository.save(owner);
		return ResponseEntity.ok(OwnerDto.fromEntity(savedOwner));
	}

	/**
	 * Delete an owner.
	 * @param ownerId owner ID
	 * @return no content
	 */
	@DeleteMapping("/{ownerId}")
	public ResponseEntity<Void> deleteOwner(@PathVariable Integer ownerId) {
		Owner owner = ownerRepository.findById(ownerId)
			.orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId));
		ownerRepository.delete(owner);
		return ResponseEntity.noContent().build();
	}

}
