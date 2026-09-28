# TabLusa backend

## Stack
- Java 25, Spring Boot 4 (Framework 7). Exact versions: pom.xml.
- PostgreSQL 18.6 (`postgres:18.6`), same tag in `compose.yaml` and `TestcontainersConfiguration`.
- Spring Data JPA (Hibernate), Liquibase, Bean Validation, Actuator, Testcontainers, MapStruct.

## Commands (Git Bash, from `backend/`)
Prerequisites: JDK 25 as `JAVA_HOME`; Docker running (tests and local runs start PostgreSQL).
- `./mvnw verify`: build and run all tests.
- `./mvnw test -Dtest=ClassName`: run one unit test class.
- `./mvnw verify -Dit.test=ClassName`: run one integration test class (unit tests also run).
- `./mvnw spring-boot:run`: run the app; `compose.yaml` starts PostgreSQL.
- `./mvnw spring-boot:test-run`: run against a Testcontainers database.

In PowerShell use `.\mvnw.cmd` with the same arguments.

## Architecture (decision 010), per feature package, e.g. `song/`
- Layout: `domain`, `application/port/in`, `application/port/out`, `application/service`, `adapter/in/web`, `adapter/in/seed`, `adapter/out/persistence`; wiring in `config/`.
- `domain` imports only `java.*` and `org.jspecify.annotations`.
- `application` imports only `java.*`, `domain` and `org.jspecify.annotations`: no Spring, no `jakarta.*`, no `@Service`/`@Component`/`@Transactional`.
- Use cases are plain classes implementing `port.in`, registered as `@Bean`s in `config/` and wrapped by a TransactionTemplate-based decorator.
- Only the transaction-decorated use case is a bean; the undecorated service is never exposed.
- Adapters never import from another adapter package.
- `jakarta.persistence` appears only in `adapter/out/persistence`.
- Controllers depend only on `port.in`: use cases take commands and return result models defined in `port.in`; controllers map those to and from web DTOs.
- Separate models per layer: domain, `port.in` commands and results, JPA entities, web DTOs. Application-layer mappers are hand-written; adapters use MapStruct (unmapped target properties fail the build).
- ChordPro parsing is an inbound adapter. Chord-name parsing and key-relative degrees are domain.
- The ChordPro parser rejects unsupported directives with an error naming the directive and line.
- Never weaken or delete an architecture test rule to make the build pass; stop and report the violation.

## Persistence (decisions 006, 009)
- Song content is one JSONB column mapped with `@JdbcTypeCode(SqlTypes.JSON)`, carrying a `schemaVersion` field. Content migrations are Java code, never Liquibase.
- Liquibase owns the relational schema only. Never edit an applied changeset.
- `unaccent` and `pg_trgm` are created by a Liquibase changeset (`CREATE EXTENSION IF NOT EXISTS`).
- Catalogue search uses native queries with `unaccent` and `pg_trgm`.
- `spring.jpa.open-in-view=false`.
- Every read use case exposed via `port.in` has a test asserting its SQL statement count.

## Idioms
- Constructor injection only: no field or setter injection, no `@Autowired`; `final` fields.
- Web DTOs and use-case commands are records.
- Sealed interfaces plus pattern-matching `switch` for closed hierarchies.
- `ProblemDetail` for error responses.
- Jackson 3 (`tools.jackson.*`); annotations stay in `com.fasterxml.jackson.annotation`.
- `@MockitoBean`/`@MockitoSpyBean` in tests.
- No preview features (no `--enable-preview`).
- Avoid: Lombok; `RestTemplate`; `WebClient` in MVC code; `@MockBean` (removed in Boot 4); `com.fasterxml.jackson.databind`; JUnit 4; `javax.persistence`, `javax.validation`, `javax.servlet`, `javax.annotation` (use `jakarta.*`).

## Testing
- Integration tests use real PostgreSQL via Testcontainers (`@Import(TestcontainersConfiguration.class)`, `@ServiceConnection`). No H2, HSQLDB or other in-memory substitutes.
- Integration tests are named `*IT`.
- Domain and application tests are plain JUnit; no Spring context.
- The ChordPro parser and chord-degree conversion need property-based tests: parse → serialise round-trip; chord name → key-relative degree → chord name in the same key returns the original, including spelling (Bb vs A#).
- The property-based testing library is pending: jqwik, named in decision 007, does not run on the JUnit Platform version Spring Boot manages. Do not add a PBT dependency or override JUnit versions; stop and ask.
