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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetType;
import org.springframework.samples.petclinic.owner.PetTypeRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test class for {@link PetApiController}.
 */
@WebMvcTest(PetApiController.class)
class PetApiControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private OwnerRepository ownerRepository;

	@MockitoBean
	private PetTypeRepository petTypeRepository;

	private Owner owner;

	private Pet pet;

	private PetType petType;

	@BeforeEach
	void setup() {
		owner = new Owner();
		owner.setId(1);
		owner.setFirstName("George");
		owner.setLastName("Franklin");
		owner.setAddress("110 W. Liberty St.");
		owner.setCity("Madison");
		owner.setTelephone("6085551023");

		petType = new PetType();
		petType.setId(1);
		petType.setName("cat");

		pet = new Pet();
		pet.setName("Leo");
		pet.setBirthDate(LocalDate.of(2020, 9, 7));
		pet.setType(petType);

		owner.addPet(pet);
		pet.setId(1); // Set ID after adding to owner
	}

	@Test
	void testGetPet() throws Exception {
		given(ownerRepository.findById(1)).willReturn(Optional.of(owner));

		mockMvc.perform(get("/api/owners/1/pets/1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(1))
			.andExpect(jsonPath("$.name").value("Leo"))
			.andExpect(jsonPath("$.type.name").value("cat"));
	}

	@Test
	void testGetPetOwnerNotFound() throws Exception {
		given(ownerRepository.findById(999)).willReturn(Optional.empty());

		mockMvc.perform(get("/api/owners/999/pets/1")).andExpect(status().isNotFound());
	}

	@Test
	void testGetPetNotFound() throws Exception {
		given(ownerRepository.findById(1)).willReturn(Optional.of(owner));

		mockMvc.perform(get("/api/owners/1/pets/999")).andExpect(status().isNotFound());
	}

	@Test
	void testCreatePet() throws Exception {
		given(ownerRepository.findById(1)).willReturn(Optional.of(owner));
		given(petTypeRepository.findById(1)).willReturn(Optional.of(petType));
		given(ownerRepository.save(any(Owner.class))).willReturn(owner);

		String petJson = """
				{
					"name": "Max",
					"birthDate": "2022-05-15",
					"type": {
						"id": 1,
						"name": "cat"
					}
				}
				""";

		mockMvc.perform(post("/api/owners/1/pets").contentType(MediaType.APPLICATION_JSON).content(petJson))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.name").value("Max"));
	}

	@Test
	void testCreatePetFutureBirthDate() throws Exception {
		given(ownerRepository.findById(1)).willReturn(Optional.of(owner));
		given(petTypeRepository.findById(1)).willReturn(Optional.of(petType));

		LocalDate futureDate = LocalDate.now().plusDays(1);
		String petJson = String.format("""
				{
					"name": "Max",
					"birthDate": "%s",
					"type": {
						"id": 1,
						"name": "cat"
					}
				}
				""", futureDate.toString());

		mockMvc.perform(post("/api/owners/1/pets").contentType(MediaType.APPLICATION_JSON).content(petJson))
			.andExpect(status().isBadRequest());
	}

	@Test
	void testCreatePetDuplicateName() throws Exception {
		given(ownerRepository.findById(1)).willReturn(Optional.of(owner));
		given(petTypeRepository.findById(1)).willReturn(Optional.of(petType));

		String petJson = """
				{
					"name": "Leo",
					"birthDate": "2022-05-15",
					"type": {
						"id": 1,
						"name": "cat"
					}
				}
				""";

		mockMvc.perform(post("/api/owners/1/pets").contentType(MediaType.APPLICATION_JSON).content(petJson))
			.andExpect(status().isBadRequest());
	}

	@Test
	void testUpdatePet() throws Exception {
		given(ownerRepository.findById(1)).willReturn(Optional.of(owner));
		given(petTypeRepository.findById(1)).willReturn(Optional.of(petType));
		given(ownerRepository.save(any(Owner.class))).willReturn(owner);

		String petJson = """
				{
					"id": 1,
					"name": "Leo Updated",
					"birthDate": "2020-09-07",
					"type": {
						"id": 1,
						"name": "cat"
					}
				}
				""";

		mockMvc.perform(put("/api/owners/1/pets/1").contentType(MediaType.APPLICATION_JSON).content(petJson))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("Leo Updated"));
	}

	@Test
	void testDeletePet() throws Exception {
		given(ownerRepository.findById(1)).willReturn(Optional.of(owner));
		given(ownerRepository.save(any(Owner.class))).willReturn(owner);

		mockMvc.perform(delete("/api/owners/1/pets/1")).andExpect(status().isNoContent());

		verify(ownerRepository, times(1)).save(owner);
	}

}
