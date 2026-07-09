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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.api.ApiExceptionHandler.ErrorResponse;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

/**
 * Test class for {@link ApiExceptionHandler}.
 */
class ApiExceptionHandlerTests {

	private ApiExceptionHandler exceptionHandler;

	@BeforeEach
	void setup() {
		exceptionHandler = new ApiExceptionHandler();
	}

	@Test
	void testHandleValidationException() {
		BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "test");
		bindingResult.addError(new FieldError("test", "field1", "Field1 is required"));
		bindingResult.addError(new FieldError("test", "field2", "Field2 must be 10 digits"));

		MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

		ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationException(ex);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(400, response.getBody().getStatus());
		assertEquals("Bad Request", response.getBody().getError());
		assertEquals("Validation failed", response.getBody().getMessage());
		assertEquals(2, response.getBody().getErrors().size());
	}

	@Test
	void testHandleIllegalArgumentExceptionNotFound() {
		IllegalArgumentException ex = new IllegalArgumentException("Owner not found with id: 123");

		ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgumentException(ex);

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(404, response.getBody().getStatus());
		assertEquals("Not Found", response.getBody().getError());
		assertEquals("Owner not found with id: 123", response.getBody().getMessage());
	}

	@Test
	void testHandleIllegalArgumentExceptionBadRequest() {
		IllegalArgumentException ex = new IllegalArgumentException("Birth date cannot be in the future");

		ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgumentException(ex);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(400, response.getBody().getStatus());
		assertEquals("Bad Request", response.getBody().getError());
	}

	@Test
	void testHandleGenericException() {
		Exception ex = new RuntimeException("Unexpected error occurred");

		ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(ex);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(500, response.getBody().getStatus());
		assertEquals("Internal Server Error", response.getBody().getError());
		assertEquals("Unexpected error occurred", response.getBody().getMessage());
	}

}
