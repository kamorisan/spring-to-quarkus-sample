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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.samples.petclinic.vet.Vet;

/**
 * DTO for Vet entity.
 */
public class VetDto {

	private Integer id;

	private String firstName;

	private String lastName;

	private List<SpecialtyDto> specialties;

	public VetDto() {
		this.specialties = new ArrayList<>();
	}

	public VetDto(Integer id, String firstName, String lastName, List<SpecialtyDto> specialties) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.specialties = specialties != null ? specialties : new ArrayList<>();
	}

	public static VetDto fromEntity(Vet vet) {
		List<SpecialtyDto> specialtyDtos = vet.getSpecialties()
			.stream()
			.map(SpecialtyDto::fromEntity)
			.collect(Collectors.toList());

		return new VetDto(vet.getId(), vet.getFirstName(), vet.getLastName(), specialtyDtos);
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public List<SpecialtyDto> getSpecialties() {
		return specialties;
	}

	public void setSpecialties(List<SpecialtyDto> specialties) {
		this.specialties = specialties;
	}

}
