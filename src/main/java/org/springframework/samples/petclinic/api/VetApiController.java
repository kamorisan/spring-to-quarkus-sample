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

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.samples.petclinic.api.dto.PagedResponse;
import org.springframework.samples.petclinic.api.dto.VetDto;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API controller for Vet resources.
 */
@RestController
@RequestMapping("/api/vets")
@CrossOrigin(origins = "http://localhost:3000")
public class VetApiController {

	private final VetRepository vetRepository;

	private static final int PAGE_SIZE = 5;

	public VetApiController(VetRepository vetRepository) {
		this.vetRepository = vetRepository;
	}

	/**
	 * Get all vets without pagination.
	 * @return list of all vets
	 */
	@GetMapping("/all")
	public List<VetDto> getAllVets() {
		return vetRepository.findAll().stream().map(VetDto::fromEntity).collect(Collectors.toList());
	}

	/**
	 * Get paginated vets.
	 * @param page page number (1-based)
	 * @return paginated response of vets
	 */
	@GetMapping
	public PagedResponse<VetDto> getVets(@RequestParam(defaultValue = "1") int page) {
		// Convert 1-based page to 0-based for Spring Data
		PageRequest pageRequest = PageRequest.of(page - 1, PAGE_SIZE);
		Page<VetDto> vetPage = vetRepository.findAll(pageRequest).map(VetDto::fromEntity);

		return new PagedResponse<>(vetPage.getContent(), page, vetPage.getTotalPages(), vetPage.getTotalElements());
	}

}
