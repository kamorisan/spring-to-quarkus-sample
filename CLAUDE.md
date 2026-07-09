# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is the Spring PetClinic sample application - a Spring Boot web application demonstrating best practices for Spring MVC, Spring Data JPA, and Thymeleaf. The project uses Maven for builds and requires Java 17 or newer.

The repository name suggests this may be a migration project from Spring to Quarkus, but currently contains the standard Spring Boot PetClinic implementation.

## Build and Run Commands

### Build the application
```bash
./mvnw clean package
```

### Run the application locally
```bash
./mvnw spring-boot:run
```
The application will be available at http://localhost:8080/

### Run with specific database profile
```bash
# MySQL
./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql

# PostgreSQL
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

### Run tests
```bash
./mvnw test
```

### Run a single test class
```bash
./mvnw test -Dtest=OwnerControllerTests
```

### Run a single test method
```bash
./mvnw test -Dtest=OwnerControllerTests#testMethod
```

### Code formatting and validation
```bash
# Validate Spring Java formatting
./mvnw spring-javaformat:validate

# Apply Spring Java formatting
./mvnw spring-javaformat:apply

# Run checkstyle
./mvnw checkstyle:check
```

### Compile CSS from SCSS
```bash
./mvnw package -P css
```
This recompiles `src/main/scss/petclinic.scss` to `src/main/resources/static/resources/css/petclinic.css` with Bootstrap.

### Build container image
```bash
./mvnw spring-boot:build-image
docker run -p 8080:8080 docker.io/library/spring-petclinic:latest
```

### Start database containers
```bash
# MySQL
docker compose up mysql

# PostgreSQL
docker compose up postgres
```

## Architecture

### Package Structure

The application follows a feature-based package organization:

- **`org.springframework.samples.petclinic`** - Root package with main application class
  - **`model`** - Base entity classes (`BaseEntity`, `NamedEntity`, `Person`)
  - **`owner`** - Owner domain: entities (`Owner`, `Pet`, `Visit`, `PetType`), repositories, controllers, validators
  - **`vet`** - Veterinarian domain: entities (`Vet`, `Specialty`), repository, controllers
  - **`system`** - System-level components: cache configuration, web configuration, welcome/error controllers

### Key Architectural Patterns

**Repository Pattern**: Uses Spring Data JPA repositories with method name conventions for query derivation.
- Example: `OwnerRepository.findByLastNameStartingWith(String lastName, Pageable pageable)`

**MVC with Thymeleaf**: Controllers return view names that map to Thymeleaf templates in `src/main/resources/templates/`.

**Package-Private Controllers**: Most controllers are package-private (not public) following Spring's recommendation for component scanning.

**Entity Inheritance**: Domain model uses inheritance:
- `BaseEntity` (id field) → `NamedEntity` (name field) → specific entities
- `Person` (extends `BaseEntity`) → `Owner`, `Vet`

**Caching**: Vets listing is cached (see `CacheConfiguration` and `@Cacheable` annotations).

**Validation**: Uses Jakarta Bean Validation annotations and custom validators (e.g., `PetValidator`).

## Database Configuration

**Default**: H2 in-memory database (for development)
- H2 console: http://localhost:8080/h2-console
- JDBC URL is printed at startup: `jdbc:h2:mem:<uuid>`

**Profiles**: Switch databases using Spring profiles (`spring.profiles.active`):
- `mysql` - MySQL database
- `postgres` - PostgreSQL database

Database schemas are in `src/main/resources/db/{h2,mysql,postgres}/`.

## Testing

### Test Application Entry Points

For rapid development feedback, run these main classes:

- **`PetClinicIntegrationTests.main()`** - Uses H2 database with Spring Boot DevTools
- **`MysqlTestApplication.main()`** - Uses MySQL via Testcontainers
- **`PostgresIntegrationTests`** - Uses PostgreSQL via Docker Compose

These classes serve dual purposes: runnable applications for development AND integration tests.

### Test Types

- **Controller tests** (`*ControllerTests`) - Use `@WebMvcTest` for slice testing
- **Integration tests** (`*IntegrationTests`) - Use `@SpringBootTest` with full context
- **Service tests** - Located in `src/test/java/.../service/`
- **Validation tests** - Test Jakarta Bean Validation and custom validators

## Code Style

This project follows Spring Framework code conventions:

- **Formatting**: Uses `spring-javaformat-maven-plugin` - run `./mvnw spring-javaformat:apply` before commits
- **Checkstyle**: Enforced via `maven-checkstyle-plugin` including nohttp validation
- **License headers**: All source files must include Apache License 2.0 header
- **Commits**: Must include `Signed-off-by` trailer (DCO requirement)

## Native Image Support

The project includes GraalVM Native Image support via:
- `native-maven-plugin` in pom.xml
- `PetClinicRuntimeHints` for runtime reflection hints
- Build: `./mvnw -Pnative native:compile`

## Actuator Endpoints

Spring Boot Actuator is enabled with all endpoints exposed (development only).
Access at: http://localhost:8080/actuator/

**Note**: In production, restrict `management.endpoints.web.exposure.include`
