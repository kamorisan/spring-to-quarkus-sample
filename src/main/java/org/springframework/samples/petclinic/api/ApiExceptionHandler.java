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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for REST API controllers.
 */
@RestControllerAdvice(basePackages = "org.springframework.samples.petclinic.api")
public class ApiExceptionHandler {

	/**
	 * Handle validation errors.
	 * @param ex validation exception
	 * @return error response
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
		List<FieldErrorDto> fieldErrors = new ArrayList<>();

		for (FieldError error : ex.getBindingResult().getFieldErrors()) {
			fieldErrors.add(new FieldErrorDto(error.getField(), error.getDefaultMessage()));
		}

		ErrorResponse errorResponse = new ErrorResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
				"Bad Request", "Validation failed", fieldErrors);

		return ResponseEntity.badRequest().body(errorResponse);
	}

	/**
	 * Handle illegal argument exceptions (resource not found, business rule violations).
	 * @param ex illegal argument exception
	 * @return error response
	 */
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
		// Determine if it's a not found or bad request based on message
		HttpStatus status = ex.getMessage().contains("not found") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;

		ErrorResponse errorResponse = new ErrorResponse(LocalDateTime.now(), status.value(), status.getReasonPhrase(),
				ex.getMessage(), null);

		return ResponseEntity.status(status).body(errorResponse);
	}

	/**
	 * Handle all other exceptions.
	 * @param ex exception
	 * @return error response
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
		ErrorResponse errorResponse = new ErrorResponse(LocalDateTime.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(),
				"Internal Server Error", ex.getMessage(), null);

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
	}

	/**
	 * Error response structure.
	 */
	public static class ErrorResponse {

		private LocalDateTime timestamp;

		private int status;

		private String error;

		private String message;

		private List<FieldErrorDto> errors;

		public ErrorResponse() {
		}

		public ErrorResponse(LocalDateTime timestamp, int status, String error, String message,
				List<FieldErrorDto> errors) {
			this.timestamp = timestamp;
			this.status = status;
			this.error = error;
			this.message = message;
			this.errors = errors;
		}

		public LocalDateTime getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(LocalDateTime timestamp) {
			this.timestamp = timestamp;
		}

		public int getStatus() {
			return status;
		}

		public void setStatus(int status) {
			this.status = status;
		}

		public String getError() {
			return error;
		}

		public void setError(String error) {
			this.error = error;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public List<FieldErrorDto> getErrors() {
			return errors;
		}

		public void setErrors(List<FieldErrorDto> errors) {
			this.errors = errors;
		}

	}

	/**
	 * Field error structure.
	 */
	public static class FieldErrorDto {

		private String field;

		private String message;

		public FieldErrorDto() {
		}

		public FieldErrorDto(String field, String message) {
			this.field = field;
			this.message = message;
		}

		public String getField() {
			return field;
		}

		public void setField(String field) {
			this.field = field;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

	}

}
