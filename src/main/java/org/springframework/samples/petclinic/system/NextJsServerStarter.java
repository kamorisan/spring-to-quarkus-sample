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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;

/**
 * Starts Next.js standalone server as an embedded process when Spring Boot application
 * starts. The Next.js standalone build is extracted from the classpath resources and
 * started using the bundled Node.js runtime.
 */
@Component
public class NextJsServerStarter implements ApplicationRunner {

	private static final Logger logger = LoggerFactory.getLogger(NextJsServerStarter.class);

	@Value("${nextjs.enabled:true}")
	private boolean enabled;

	@Value("${nextjs.port:3000}")
	private int port;

	private Process nextJsProcess;

	private Path workingDir;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		if (!enabled) {
			logger.info("Next.js server is disabled");
			return;
		}

		try {
			// Extract Next.js standalone from classpath
			workingDir = extractNextJsStandalone();

			// Check if Node.js is available
			if (!isNodeJsAvailable()) {
				logger.warn("Node.js is not available. Next.js server will not start.");
				logger.warn("Please install Node.js or run Next.js separately: cd petclinic-frontend && npm run dev");
				return;
			}

			// Start Next.js server
			startNextJsServer();

			logger.info("Next.js server started on port {}", port);
		}
		catch (Exception e) {
			logger.error("Failed to start Next.js server", e);
			throw e;
		}
	}

	private Path extractNextJsStandalone() throws IOException {
		logger.info("Extracting Next.js standalone build from classpath...");

		// Create temporary directory
		Path tempDir = Files.createTempDirectory("nextjs-standalone-");
		logger.info("Working directory: {}", tempDir);

		// Extract all resources from classpath:/nextjs/**
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		Resource[] resources = resolver.getResources("classpath:nextjs/**");

		for (Resource resource : resources) {
			String resourcePath = resource.getURL().toString();
			String relativePath = resourcePath.substring(resourcePath.indexOf("nextjs/") + 7);

			if (relativePath.isEmpty()) {
				continue;
			}

			// Decode URL-encoded characters (e.g., %5B -> [, %5D -> ])
			String decodedPath = URLDecoder.decode(relativePath, StandardCharsets.UTF_8);
			Path targetPath = tempDir.resolve(decodedPath);

			// Create parent directories
			Files.createDirectories(targetPath.getParent());

			// Copy file
			if (!resourcePath.endsWith("/")) {
				try (InputStream is = resource.getInputStream()) {
					Files.copy(is, targetPath, StandardCopyOption.REPLACE_EXISTING);
				}
			}
		}

		logger.info("Next.js standalone extracted to: {}", tempDir);
		return tempDir;
	}

	private boolean isNodeJsAvailable() {
		try {
			Process process = new ProcessBuilder("node", "--version").start();
			int exitCode = process.waitFor();
			if (exitCode == 0) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String version = reader.readLine();
				logger.info("Node.js version: {}", version);
				return true;
			}
		}
		catch (Exception e) {
			logger.debug("Node.js check failed", e);
		}
		return false;
	}

	private void startNextJsServer() throws IOException {
		File serverJs = workingDir.resolve("server.js").toFile();

		if (!serverJs.exists()) {
			throw new IOException("server.js not found in: " + workingDir);
		}

		ProcessBuilder pb = new ProcessBuilder("node", "server.js");
		pb.directory(workingDir.toFile());
		pb.environment().put("PORT", String.valueOf(port));
		pb.environment().put("HOSTNAME", "0.0.0.0");

		// Redirect output to logger
		pb.redirectErrorStream(true);

		nextJsProcess = pb.start();

		// Read output in separate thread
		Thread outputThread = new Thread(() -> {
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(nextJsProcess.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					logger.info("[Next.js] {}", line);
				}
			}
			catch (IOException e) {
				logger.error("Error reading Next.js output", e);
			}
		});
		outputThread.setDaemon(true);
		outputThread.start();

		// Wait a moment to check if process started successfully
		try {
			Thread.sleep(2000);
			if (!nextJsProcess.isAlive()) {
				throw new IOException("Next.js process terminated immediately");
			}
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	@PreDestroy
	public void cleanup() {
		logger.info("Stopping Next.js server...");

		if (nextJsProcess != null && nextJsProcess.isAlive()) {
			nextJsProcess.destroy();
			try {
				nextJsProcess.waitFor();
				logger.info("Next.js server stopped");
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				nextJsProcess.destroyForcibly();
			}
		}

		// Clean up temporary directory
		if (workingDir != null && Files.exists(workingDir)) {
			try (Stream<Path> walk = Files.walk(workingDir)) {
				walk.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
				logger.info("Cleaned up working directory: {}", workingDir);
			}
			catch (IOException e) {
				logger.warn("Failed to clean up working directory", e);
			}
		}
	}

}
