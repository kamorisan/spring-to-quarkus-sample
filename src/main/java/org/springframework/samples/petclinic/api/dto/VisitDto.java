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

import org.springframework.samples.petclinic.owner.Visit;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for Visit entity.
 */
public class VisitDto {

	private Integer id;

	@NotNull(message = "Visit date is required")
	private LocalDate date;

	@NotBlank(message = "Description is required")
	private String description;

	public VisitDto() {
	}

	public VisitDto(Integer id, LocalDate date, String description) {
		this.id = id;
		this.date = date;
		this.description = description;
	}

	public static VisitDto fromEntity(Visit visit) {
		return new VisitDto(visit.getId(), visit.getDate(), visit.getDescription());
	}

	public Visit toEntity() {
		Visit visit = new Visit();
		visit.setId(this.id);
		visit.setDate(this.date);
		visit.setDescription(this.description);
		return visit;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
