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
package org.springframework.samples.petclinic.api.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.samples.petclinic.owner.Pet;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for Pet entity.
 */
public class PetDto {

	private Integer id;

	@NotBlank(message = "Pet name is required")
	private String name;

	@NotNull(message = "Birth date is required")
	private LocalDate birthDate;

	// Read-only: included in responses, ignored in requests
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private PetTypeDto type;

	// Write-only: required in requests, excluded from responses
	@NotNull(message = "Pet type is required")
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private Integer petTypeId;

	private List<VisitDto> visits;

	public PetDto() {
		this.visits = new ArrayList<>();
	}

	public PetDto(Integer id, String name, LocalDate birthDate, PetTypeDto type, List<VisitDto> visits) {
		this.id = id;
		this.name = name;
		this.birthDate = birthDate;
		this.type = type;
		this.visits = visits != null ? visits : new ArrayList<>();
	}

	public static PetDto fromEntity(Pet pet) {
		List<VisitDto> visitDtos = pet.getVisits().stream().map(VisitDto::fromEntity).collect(Collectors.toList());

		return new PetDto(pet.getId(), pet.getName(), pet.getBirthDate(), PetTypeDto.fromEntity(pet.getType()),
				visitDtos);
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public LocalDate getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(LocalDate birthDate) {
		this.birthDate = birthDate;
	}

	public PetTypeDto getType() {
		return type;
	}

	public void setType(PetTypeDto type) {
		this.type = type;
	}

	public List<VisitDto> getVisits() {
		return visits;
	}

	public void setVisits(List<VisitDto> visits) {
		this.visits = visits;
	}

	public Integer getPetTypeId() {
		return petTypeId;
	}

	public void setPetTypeId(Integer petTypeId) {
		this.petTypeId = petTypeId;
	}

}
