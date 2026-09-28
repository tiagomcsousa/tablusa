# Decisions

## 001 — Scope of Phase 1 and copyrighted content
**Date:** 2026-09-26
**Status:** Accepted

**Decision:** Phase 1 is a portfolio project. No public deployment of copyrighted
content (lyrics, tabs of protected works). Licensing will be reassessed before any
public launch.

**Alternatives considered:** chords/tabs only without lyrics; licensing upfront.

**Consequences:** Seed/demo data must use public-domain songs, original compositions
or clearly fictional content. The data model should not assume licensing constraints
yet, but should not make them hard to add later (e.g. per-song licence/source metadata).

## 002 — Hybrid content model
**Date:** 2026-09-26
**Status:** Accepted

**Decision:** Community members submit chord sheets/tabs and fulfil song requests;
a moderator reviews submissions before they are published. Requests can be voted on.

**Alternatives considered:** author-only curation (quality, but doesn't scale);
open community without review (scales, but no quality control).

**Consequences:** The domain needs user roles (user, moderator/admin), a submission
lifecycle (e.g. draft → pending review → published / rejected), request voting, and
notifications to both requesters and submitters.

## 003 — Content format: input formats separate from internal model
**Date:** 2026-09-26
**Status:** Accepted

**Decision:** Chord sheets are stored and rendered from an internal structured model
(song → sections → lines → segments of chord + lyric fragment; chords as structured
objects, stored relative to the song's key). Input formats are parsed into this model
and are not the source of truth.

**Alternatives considered:** plain text with chords above lyrics (easy to author, but
fragile transposition and poor mobile layout); ChordPro as the storage format (standard
and well supported, but limited semantics); fully custom model with no standard input
(most flexible, but requires building all tooling from scratch).

**Consequences:** Transposition and capo become rendering concerns. The model enables
future features (chord simplification, section-aware views, tempo-based auto-scroll)
without migrating content. Phase 1 seed content is authored in ChordPro and parsed into
the model; paste-import of chords-over-lyrics text is planned for the contribution flow
(Phase 2). Parser and transposition logic must be covered by property-based tests
(e.g. transposing +12 or +n then −n returns the original).

## 004 — Phase 1 scope: consultation only
**Date:** 2026-09-26
**Status:** Accepted

**Decision:** Phase 1 delivers read-only consultation of chord sheets. Community
features (decision 002) come in Phase 2.

In scope:
- Song catalogue with search by title and artist.
- Song page with chords over lyrics, mobile-first.
- Transposition (up/down by semitone) and capo.
- Chord diagrams for the chords used in each song.
- Tab blocks inside chord sheets, rendered as preformatted text: not parsed and not
  transposed; the UI must make clear they stay in the original key when the sheet is
  transposed.
- 10–20 seed songs (public domain or original, see 001), authored as ChordPro files
  in the repository.

Out of scope:
- User accounts, favourites, submissions, requests and notifications (Phase 2).
- Auto-scroll and stage mode (early candidates after Phase 1).
- Structured, transposable tablature.

**Alternatives considered:** consultation plus a thin community loop in one phase
(shows the differentiator earlier, but much more domain before anything is usable);
tablature fully out (simpler, but some songs incomplete); structured tablature in
Phase 1 (complete, but costly).

**Consequences:** Phase 1 does not yet demonstrate the community differentiator.
No authentication is needed in Phase 1.

## 005 — Application stack: Angular + Spring Boot
**Date:** 2026-09-26
**Status:** Accepted (amended by 007)

**Decision:** Angular (latest major) for the frontend, Spring Boot 4.x for the backend.
Explicit goal: adopt current idioms of both (Angular signals, zoneless, new control flow;
Spring Boot 4 / Framework 7 APIs) rather than reproducing older patterns.

**Alternatives considered:** React/Next.js + Spring (new frontend skills, but weaker
ability to review AI output); Angular + Kotlin/Spring (small learning gain, low visibility).

**Consequences:** Rendering and transposition live in the frontend (TypeScript), working
from the structured model; ChordPro parsing lives in the backend (see 007). Modern-idiom conventions must be encoded in CLAUDE.md and enforced
by lint/architecture checks where possible, since AI tools default to older patterns.

## 006 — Database: PostgreSQL
**Date:** 2026-09-26
**Status:** Accepted

**Decision:** PostgreSQL. Relational schema for the catalogue and, in Phase 2, users,
submissions, requests and votes; song content (decision 003 model) stored as JSONB.

**Alternatives considered:** MySQL (known; JSON type available, but weaker JSON indexing
and text search); MongoDB (fits song documents, but Phase 2's domain is relational);
DynamoDB (catalogue search and ad hoc listings would need complex single-table design
and a separate search service).

**Consequences:** Accent-insensitive, typo-tolerant search via the unaccent and pg_trgm
extensions, with no separate search service in Phase 1. Liquibase manages the relational
schema only; song documents carry a schemaVersion field and are migrated in application
code. Integration tests run against real PostgreSQL via Testcontainers (no in-memory
substitutes). CLAUDE.md must state the database and its version.

## 007 — ChordPro parsing in the backend
**Date:** 2026-09-26
**Status:** Accepted

**Decision:** A ChordPro parser for a defined subset (metadata, sections, inline chords,
tab blocks, comments) is implemented in Java in the backend. It is used for seed
ingestion in Phase 1 and for validating submissions in Phase 2. The frontend receives
the structured model only and never parses ChordPro.

**Alternatives considered:** build-time TypeScript script with chordsheetjs (fast, but
leads to two parsers once Phase 2 needs server-side validation); Node parsing service
(single parser, but an extra runtime for little gain); parsing in the frontend at render
time (contradicts decision 003).

**Consequences:** Unsupported directives are rejected with clear errors. The parser is
specified first (supported subset, example files, properties) and then implemented,
with property-based tests (jqwik), e.g. parse → serialise round-trip and transposition
invariants. Frontend chord spelling depends on the key (e.g. Bb vs A#) and needs its own
tests. Decision 005 is amended: the frontend does not need a ChordPro library.

## 008 — Monorepo
**Date:** 2026-09-26
**Status:** Accepted

**Decision:** A single repository with `backend/` and `frontend/` folders, `docs/` for the
product vision and decisions, and layered CLAUDE.md files (root, backend, frontend).
No monorepo build tooling (Nx, Turborepo); each side uses its native build tool.

**Alternatives considered:** separate repositories for backend and frontend (independent
history and CI, but cross-cutting changes span two repos and an AI agent sees only half
the system; suits separate teams, not a solo project); monorepo tooling (unnecessary
overhead for two projects).

**Consequences:** CI pipelines use path filters per folder. `docs/` in the repository
becomes the source of truth for decisions and product vision; Project knowledge files
are copies. Vertical slices (API, contract and UI) are implemented and reviewed as a
single change.

## 009 — Persistence: Spring Data JPA (Hibernate)
**Date:** 2026-09-27
**Status:** Accepted

**Decision:** Spring Data JPA with Hibernate for persistence. Song content (decision 003
model) is stored as JSONB, mapped with `@JdbcTypeCode(SqlTypes.JSON)`. Catalogue search
(unaccent, pg_trgm) uses native queries.

**Alternatives considered:** JdbcClient (explicit SQL and full PostgreSQL control, but
more mapping code); Spring Data JDBC (aggregate-oriented, no lazy loading, but custom
JSONB converters).

**Consequences:** `spring.jpa.open-in-view` is disabled. JPA entities live only in the
persistence adapter (decision 010). Tests for key use cases assert the number of SQL
statements executed, to catch N+1 problems that would not be visible in a code review.

## 010 — Architecture: strict Clean Architecture (ports and adapters)
**Date:** 2026-09-27
**Status:** Accepted (amended by 011 and 012)

**Decision:** The backend applies Clean Architecture uniformly, implemented as ports and
adapters and organised by feature (e.g. `song/`), each with `domain`, `application`
(`port/in`, `port/out`, `service`) and `adapter` (`in/web`, `in/seed`,
`out/persistence`) packages. Strict variant: the application layer has no Spring
dependencies; use cases are plain Java classes wired as beans in `config/`, and
transactions are handled outside them (e.g. a TransactionTemplate-based decorator).

**Alternatives considered:** pragmatic Clean Architecture (pure domain core, ports only
where there are rules, direct read paths; less ceremony); Spring annotations allowed in
the application layer (simpler, but relaxes the dependency rule); traditional layers
(controller/service/repository).

**Motivation:** learning goal: apply Clean Architecture rigorously and observe its real
costs, rather than choosing the lowest-ceremony option.

**Consequences:** Separate models per layer (domain, `port.in` commands and results,
JPA entities, web DTOs) with explicit mappers (see 011). ChordPro parsing is an inbound
adapter; chord semantics (chord-name parsing, key-relative degrees) are domain.
Dependency rules are enforced by ArchUnit tests in the build: `domain` depends only on
`java.*` and `org.jspecify.annotations` (see 012); `application` only on `domain`,
`java.*` and `org.jspecify.annotations`; adapters do not depend on each other;
`jakarta.persistence` appears only in the persistence adapter; controllers depend only
on `port.in`. To be revisited at the end of Phase 1, recording the costs observed and
whether the pragmatic variant would fit better.

## 011 — Use-case boundary models and mapping
**Date:** 2026-09-28
**Status:** Accepted

**Decision:** Use cases take commands and return dedicated result models, both defined in
`port.in`. Mapping from domain to result models is hand-written in the application layer.
Adapters (web, persistence) use MapStruct for their mappings, with
`unmappedTargetPolicy=ERROR` so an unmapped target property fails the build.

**Alternatives considered:** return domain types from use cases (fewer models, but
controllers would depend on `domain`, contradicting decision 010's rule that controllers
depend only on `port.in`); hand-written mappers everywhere (no generated code, but more
boilerplate in adapters where mappings are mostly one-to-one); MapStruct in the
application layer (rejected: its generated code and annotations would add a framework
dependency to the application layer, violating decision 010).

**Consequences:** Each use case has one extra mapping (domain → result model) compared
with returning domain types; this cost is recorded for the end-of-Phase-1 review of
decision 010. Generated adapter mappers are Spring components
(`mapstruct.defaultComponentModel=spring`). Decision 010 is amended: `port.in` commands
and results are added to its list of per-layer models.

## 012 — Nullability: JSpecify in all layers
**Date:** 2026-09-28
**Status:** Accepted

**Decision:** JSpecify annotations (`@NullMarked`, `@Nullable` from
`org.jspecify.annotations`) may be used in every layer, including `domain` and
`application`. JSpecify is declared as a direct dependency, with the version managed by
Spring Boot.

**Alternatives considered:** no nullability annotations in `domain` and `application`
(keeps the core strictly `java.*`-only, but leaves null contracts undocumented where the
rules live); nullability by convention only (e.g. `Optional` for absent return values,
no annotations; nothing a tool can check).

**Consequences:** Decision 010 is amended: `domain` and `application` may also depend on
`org.jspecify.annotations`, and the ArchUnit rules allow that package and no other
non-`java.*` dependency. The annotations carry no runtime behaviour, so the core stays
framework-free; they match the null-safety annotations Spring Framework 7 uses. No
static null checker (e.g. NullAway) is adopted yet.