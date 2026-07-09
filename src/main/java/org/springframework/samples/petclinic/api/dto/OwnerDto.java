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

import org.springframework.samples.petclinic.owner.Owner;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO for Owner entity.
 */
public class OwnerDto {

	private Integer id;

	@NotBlank(message = "First name is required")
	@Size(max = 30, message = "First name must be 30 characters or less")
	private String firstName;

	@NotBlank(message = "Last name is required")
	@Size(max = 30, message = "Last name must be 30 characters or less")
	private String lastName;

	@NotBlank(message = "Address is required")
	private String address;

	@NotBlank(message = "City is required")
	private String city;

	@NotBlank(message = "Telephone is required")
	@Pattern(regexp = "\\d{10}", message = "Telephone must be exactly 10 digits")
	private String telephone;

	private List<PetDto> pets;

	public OwnerDto() {
		this.pets = new ArrayList<>();
	}

	public OwnerDto(Integer id, String firstName, String lastName, String address, String city, String telephone,
			List<PetDto> pets) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.address = address;
		this.city = city;
		this.telephone = telephone;
		this.pets = pets != null ? pets : new ArrayList<>();
	}

	public static OwnerDto fromEntity(Owner owner) {
		List<PetDto> petDtos = owner.getPets().stream().map(PetDto::fromEntity).collect(Collectors.toList());

		return new OwnerDto(owner.getId(), owner.getFirstName(), owner.getLastName(), owner.getAddress(),
				owner.getCity(), owner.getTelephone(), petDtos);
	}

	public Owner toEntity() {
		Owner owner = new Owner();
		owner.setId(this.id);
		owner.setFirstName(this.firstName);
		owner.setLastName(this.lastName);
		owner.setAddress(this.address);
		owner.setCity(this.city);
		owner.setTelephone(this.telephone);
		return owner;
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

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	public List<PetDto> getPets() {
		return pets;
	}

	public void setPets(List<PetDto> pets) {
		this.pets = pets;
	}

}
