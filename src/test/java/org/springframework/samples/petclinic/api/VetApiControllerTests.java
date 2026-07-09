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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test class for {@link VetApiController}.
 */
@WebMvcTest(VetApiController.class)
class VetApiControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private VetRepository vetRepository;

	private List<Vet> vets;

	@BeforeEach
	void setup() {
		vets = new ArrayList<>();
		Vet vet1 = new Vet();
		vet1.setId(1);
		vet1.setFirstName("James");
		vet1.setLastName("Carter");
		vets.add(vet1);

		Vet vet2 = new Vet();
		vet2.setId(2);
		vet2.setFirstName("Helen");
		vet2.setLastName("Leary");
		vets.add(vet2);
	}

	@Test
	void testGetAllVets() throws Exception {
		given(vetRepository.findAll()).willReturn(vets);

		mockMvc.perform(get("/api/vets/all"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$.length()").value(2))
			.andExpect(jsonPath("$[0].firstName").value("James"))
			.andExpect(jsonPath("$[1].firstName").value("Helen"));
	}

	@Test
	void testGetVetsPaginated() throws Exception {
		given(vetRepository.findAll(any(Pageable.class))).willReturn(new PageImpl<>(vets));

		mockMvc.perform(get("/api/vets").param("page", "1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content").isArray())
			.andExpect(jsonPath("$.content.length()").value(2))
			.andExpect(jsonPath("$.currentPage").value(1))
			.andExpect(jsonPath("$.totalPages").value(1))
			.andExpect(jsonPath("$.totalItems").value(2));
	}

	@Test
	void testGetVetsPaginatedDefaultPage() throws Exception {
		given(vetRepository.findAll(any(Pageable.class))).willReturn(new PageImpl<>(vets));

		mockMvc.perform(get("/api/vets")).andExpect(status().isOk()).andExpect(jsonPath("$.currentPage").value(1));
	}

}
