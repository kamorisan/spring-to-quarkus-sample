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
package org.springframework.samples.petclinic.system;

import java.io.IOException;
import java.net.URI;
import java.util.Enumeration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Reverse proxy controller that forwards requests to Next.js development server. This
 * allows the Spring Boot application to serve both the REST API and the Next.js frontend
 * from a single port (8080).
 *
 * Requests to /api/** are handled by Spring Boot REST controllers. All other requests
 * (except static resources and Thymeleaf pages) are proxied to Next.js running on port
 * 3000.
 */
@Controller
public class NextJsProxyController {

	@Value("${nextjs.url:http://localhost:3000}")
	private String nextJsUrl;

	private final RestTemplate restTemplate = new RestTemplate();

	/**
	 * Redirect /vets to /api/vets for compatibility with original Spring MVC routes.
	 * @param response HTTP response
	 * @throws IOException if redirect fails
	 */
	@RequestMapping(value = "/vets")
	public void redirectVets(HttpServletResponse response) throws IOException {
		response.sendRedirect("/api/vets");
	}

	/**
	 * Proxy /vets.html to /vets-list for compatibility with original Spring MVC routes.
	 * This rewrites the URL internally without redirecting the browser.
	 * @param request HTTP request
	 * @param response HTTP response
	 * @throws IOException if proxy fails
	 */
	@RequestMapping(value = "/vets.html")
	public void proxyVetsHtml(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String queryString = request.getQueryString();
		String targetUrl = nextJsUrl + "/vets-list" + (queryString != null ? "?" + queryString : "");

		try {
			// Copy headers from original request
			HttpHeaders headers = new HttpHeaders();
			Enumeration<String> headerNames = request.getHeaderNames();
			while (headerNames.hasMoreElements()) {
				String headerName = headerNames.nextElement();
				if (!"host".equalsIgnoreCase(headerName)) {
					headers.add(headerName, request.getHeader(headerName));
				}
			}

			// Forward request to Next.js /vets-list
			HttpEntity<String> entity = new HttpEntity<>(headers);
			ResponseEntity<byte[]> nextJsResponse = restTemplate.exchange(URI.create(targetUrl), HttpMethod.GET, entity,
					byte[].class);

			// Copy response status
			response.setStatus(nextJsResponse.getStatusCode().value());

			// Copy response headers (except Transfer-Encoding)
			nextJsResponse.getHeaders().forEach((name, values) -> {
				if (!"Transfer-Encoding".equalsIgnoreCase(name)) {
					values.forEach(value -> response.addHeader(name, value));
				}
			});

			// Copy response body
			if (nextJsResponse.getBody() != null) {
				response.getOutputStream().write(nextJsResponse.getBody());
			}
		}
		catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			response.setContentType("text/html");
			response.getWriter().write("Error proxying to Next.js: " + e.getMessage());
		}
	}

	/**
	 * Proxy all requests except: - /api/** (REST API handled by Spring Boot) -
	 * /resources/** (static resources) - /webjars/** (WebJars) - /*.html (Thymeleaf
	 * pages)
	 * @param request HTTP request
	 * @param response HTTP response
	 * @throws IOException if proxy fails
	 */
	@RequestMapping(value = { "/", "/owners/**", "/vets-list/**", "/oups/**", "/_next/**", "/images/**" })
	public void proxyToNextJs(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String requestUrl = request.getRequestURI();
		String queryString = request.getQueryString();
		String targetUrl = nextJsUrl + requestUrl + (queryString != null ? "?" + queryString : "");

		try {
			// Copy headers from original request
			HttpHeaders headers = new HttpHeaders();
			Enumeration<String> headerNames = request.getHeaderNames();
			while (headerNames.hasMoreElements()) {
				String headerName = headerNames.nextElement();
				// Skip host header as it will be set by RestTemplate
				if (!"host".equalsIgnoreCase(headerName)) {
					headers.add(headerName, request.getHeader(headerName));
				}
			}

			// Forward request to Next.js
			HttpEntity<String> entity = new HttpEntity<>(headers);
			ResponseEntity<byte[]> nextJsResponse = restTemplate.exchange(URI.create(targetUrl), HttpMethod.GET, entity,
					byte[].class);

			// Copy response status
			response.setStatus(nextJsResponse.getStatusCode().value());

			// Copy response headers (except Transfer-Encoding to avoid chunked encoding
			// issues)
			nextJsResponse.getHeaders().forEach((name, values) -> {
				// Skip Transfer-Encoding header as it can cause
				// ERR_INVALID_CHUNKED_ENCODING
				if (!"Transfer-Encoding".equalsIgnoreCase(name)) {
					values.forEach(value -> response.addHeader(name, value));
				}
			});

			// Copy response body
			if (nextJsResponse.getBody() != null) {
				response.getOutputStream().write(nextJsResponse.getBody());
			}
		}
		catch (Exception e) {
			// If Next.js is not running, return a helpful error message
			response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			response.setContentType("text/html");
			response.getWriter().write("""
					<!DOCTYPE html>
					<html>
					<head>
					    <title>Next.js Not Available</title>
					    <style>
					        body { font-family: sans-serif; padding: 50px; text-align: center; }
					        h1 { color: #d32f2f; }
					        code { background: #f5f5f5; padding: 2px 6px; border-radius: 3px; }
					    </style>
					</head>
					<body>
					    <h1>Next.js Frontend Not Available</h1>
					    <p>The Next.js development server is not running.</p>
					    <p>Please start it with: <code>cd petclinic-frontend && npm run dev</code></p>
							    <hr>
					    <p><small>Error: """ + e.getMessage() + "</small></p>\n" + """
					</body>
					</html>
					""");
		}
	}

}
