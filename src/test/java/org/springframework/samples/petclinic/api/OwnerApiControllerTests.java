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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test class for {@link OwnerApiController}.
 */
@WebMvcTest(OwnerApiController.class)
class OwnerApiControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private OwnerRepository ownerRepository;

	private Owner owner;

	@BeforeEach
	void setup() {
		owner = new Owner();
		owner.setId(1);
		owner.setFirstName("George");
		owner.setLastName("Franklin");
		owner.setAddress("110 W. Liberty St.");
		owner.setCity("Madison");
		owner.setTelephone("6085551023");
	}

	@Test
	void testSearchOwners() throws Exception {
		List<Owner> owners = new ArrayList<>();
		owners.add(owner);
		given(ownerRepository.findByLastNameStartingWith(any(String.class), any(Pageable.class)))
			.willReturn(new PageImpl<>(owners));

		mockMvc.perform(get("/api/owners").param("lastName", "Franklin").param("page", "1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content").isArray())
			.andExpect(jsonPath("$.content.length()").value(1))
			.andExpect(jsonPath("$.content[0].firstName").value("George"))
			.andExpect(jsonPath("$.currentPage").value(1));
	}

	@Test
	void testSearchOwnersDefaultParams() throws Exception {
		List<Owner> owners = new ArrayList<>();
		owners.add(owner);
		given(ownerRepository.findByLastNameStartingWith(eq(""), any(Pageable.class)))
			.willReturn(new PageImpl<>(owners));

		mockMvc.perform(get("/api/owners")).andExpect(status().isOk()).andExpect(jsonPath("$.content").isArray());
	}

	@Test
	void testGetOwnerFound() throws Exception {
		given(ownerRepository.findById(1)).willReturn(Optional.of(owner));

		mockMvc.perform(get("/api/owners/1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(1))
			.andExpect(jsonPath("$.firstName").value("George"))
			.andExpect(jsonPath("$.lastName").value("Franklin"))
			.andExpect(jsonPath("$.address").value("110 W. Liberty St."))
			.andExpect(jsonPath("$.city").value("Madison"))
			.andExpect(jsonPath("$.telephone").value("6085551023"));
	}

	@Test
	void testGetOwnerNotFound() throws Exception {
		given(ownerRepository.findById(999)).willReturn(Optional.empty());

		mockMvc.perform(get("/api/owners/999")).andExpect(status().isNotFound());
	}

	@Test
	void testCreateOwner() throws Exception {
		Owner newOwner = new Owner();
		newOwner.setId(2);
		newOwner.setFirstName("Betty");
		newOwner.setLastName("Davis");
		newOwner.setAddress("638 Cardinal Ave.");
		newOwner.setCity("Sun Prairie");
		newOwner.setTelephone("6085551749");

		given(ownerRepository.save(any(Owner.class))).willReturn(newOwner);

		String ownerJson = """
				{
					"firstName": "Betty",
					"lastName": "Davis",
					"address": "638 Cardinal Ave.",
					"city": "Sun Prairie",
					"telephone": "6085551749"
				}
				""";

		mockMvc.perform(post("/api/owners").contentType(MediaType.APPLICATION_JSON).content(ownerJson))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").value(2))
			.andExpect(jsonPath("$.firstName").value("Betty"));
	}

	@Test
	void testCreateOwnerValidationFails() throws Exception {
		String invalidOwnerJson = """
				{
					"firstName": "",
					"lastName": "Davis",
					"address": "638 Cardinal Ave.",
					"city": "Sun Prairie",
					"telephone": "invalid"
				}
				""";

		mockMvc.perform(post("/api/owners").contentType(MediaType.APPLICATION_JSON).content(invalidOwnerJson))
			.andExpect(status().isBadRequest());
	}

	@Test
	void testUpdateOwner() throws Exception {
		given(ownerRepository.findById(1)).willReturn(Optional.of(owner));
		given(ownerRepository.save(any(Owner.class))).willReturn(owner);

		String ownerJson = """
				{
					"id": 1,
					"firstName": "George",
					"lastName": "Franklin Jr.",
					"address": "110 W. Liberty St.",
					"city": "Madison",
					"telephone": "6085551023"
				}
				""";

		mockMvc.perform(put("/api/owners/1").contentType(MediaType.APPLICATION_JSON).content(ownerJson))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(1));
	}

	@Test
	void testUpdateOwnerNotFound() throws Exception {
		given(ownerRepository.findById(999)).willReturn(Optional.empty());

		String ownerJson = """
				{
					"id": 999,
					"firstName": "George",
					"lastName": "Franklin",
					"address": "110 W. Liberty St.",
					"city": "Madison",
					"telephone": "6085551023"
				}
				""";

		mockMvc.perform(put("/api/owners/999").contentType(MediaType.APPLICATION_JSON).content(ownerJson))
			.andExpect(status().isNotFound());
	}

	@Test
	void testUpdateOwnerIdMismatch() throws Exception {
		given(ownerRepository.findById(1)).willReturn(Optional.of(owner));

		String ownerJson = """
				{
					"id": 2,
					"firstName": "George",
					"lastName": "Franklin",
					"address": "110 W. Liberty St.",
					"city": "Madison",
					"telephone": "6085551023"
				}
				""";

		mockMvc.perform(put("/api/owners/1").contentType(MediaType.APPLICATION_JSON).content(ownerJson))
			.andExpect(status().isBadRequest());
	}

	@Test
	void testDeleteOwner() throws Exception {
		given(ownerRepository.findById(1)).willReturn(Optional.of(owner));

		mockMvc.perform(delete("/api/owners/1")).andExpect(status().isNoContent());

		verify(ownerRepository, times(1)).delete(owner);
	}

	@Test
	void testDeleteOwnerNotFound() throws Exception {
		given(ownerRepository.findById(999)).willReturn(Optional.empty());

		mockMvc.perform(delete("/api/owners/999")).andExpect(status().isNotFound());
	}

}
