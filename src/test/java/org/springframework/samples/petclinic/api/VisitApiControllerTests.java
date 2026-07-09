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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.samples.petclinic.owner.Visit;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test class for {@link VisitApiController}.
 */
@WebMvcTest(VisitApiController.class)
class VisitApiControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private OwnerRepository ownerRepository;

	private Owner owner;

	private Pet pet;

	@BeforeEach
	void setup() {
		owner = new Owner();
		owner.setId(1);
		owner.setFirstName("George");
		owner.setLastName("Franklin");
		owner.setAddress("110 W. Liberty St.");
		owner.setCity("Madison");
		owner.setTelephone("6085551023");

		PetType petType = new PetType();
		petType.setId(1);
		petType.setName("cat");

		pet = new Pet();
		pet.setName("Leo");
		pet.setBirthDate(LocalDate.of(2020, 9, 7));
		pet.setType(petType);

		Visit visit = new Visit();
		visit.setDate(LocalDate.of(2023, 1, 4));
		visit.setDescription("rabies shot");

		pet.addVisit(visit);
		owner.addPet(pet);
		pet.setId(1); // Set ID after adding to owner
		visit.setId(1); // Set ID after adding to pet
	}

	@Test
	void testGetVisits() throws Exception {
		given(ownerRepository.findById(1)).willReturn(Optional.of(owner));

		mockMvc.perform(get("/api/owners/1/pets/1/visits"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].description").value("rabies shot"));
	}

	@Test
	void testGetVisitsOwnerNotFound() throws Exception {
		given(ownerRepository.findById(999)).willReturn(Optional.empty());

		mockMvc.perform(get("/api/owners/999/pets/1/visits")).andExpect(status().isNotFound());
	}

	@Test
	void testGetVisitsPetNotFound() throws Exception {
		given(ownerRepository.findById(1)).willReturn(Optional.of(owner));

		mockMvc.perform(get("/api/owners/1/pets/999/visits")).andExpect(status().isNotFound());
	}

	@Test
	void testCreateVisit() throws Exception {
		given(ownerRepository.findById(1)).willReturn(Optional.of(owner));
		given(ownerRepository.save(any(Owner.class))).willReturn(owner);

		LocalDate futureDate = LocalDate.now().plusDays(5);
		String visitJson = String.format("""
				{
					"date": "%s",
					"description": "checkup"
				}
				""", futureDate.toString());

		mockMvc.perform(post("/api/owners/1/pets/1/visits").contentType(MediaType.APPLICATION_JSON).content(visitJson))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.description").value("checkup"));
	}

	@Test
	void testCreateVisitPastDate() throws Exception {
		given(ownerRepository.findById(1)).willReturn(Optional.of(owner));

		LocalDate pastDate = LocalDate.now().minusDays(1);
		String visitJson = String.format("""
				{
					"date": "%s",
					"description": "checkup"
				}
				""", pastDate.toString());

		mockMvc.perform(post("/api/owners/1/pets/1/visits").contentType(MediaType.APPLICATION_JSON).content(visitJson))
			.andExpect(status().isBadRequest());
	}

	@Test
	void testCreateVisitTodayDate() throws Exception {
		given(ownerRepository.findById(1)).willReturn(Optional.of(owner));

		LocalDate todayDate = LocalDate.now();
		String visitJson = String.format("""
				{
					"date": "%s",
					"description": "checkup"
				}
				""", todayDate.toString());

		// Today's date is not after today, so it should fail
		mockMvc.perform(post("/api/owners/1/pets/1/visits").contentType(MediaType.APPLICATION_JSON).content(visitJson))
			.andExpect(status().isBadRequest());
	}

	@Test
	void testCreateVisitEmptyDescription() throws Exception {
		given(ownerRepository.findById(1)).willReturn(Optional.of(owner));

		LocalDate futureDate = LocalDate.now().plusDays(5);
		String visitJson = String.format("""
				{
					"date": "%s",
					"description": ""
				}
				""", futureDate.toString());

		mockMvc.perform(post("/api/owners/1/pets/1/visits").contentType(MediaType.APPLICATION_JSON).content(visitJson))
			.andExpect(status().isBadRequest());
	}

}
