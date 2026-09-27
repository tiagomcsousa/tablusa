# TabLusa

Chord sheet and tab portal focused on Portuguese music. Personal portfolio project.
Phase 1 is read-only consultation: catalogue, song page, transposition, capo, chord
diagrams (see docs/decisions.md, decision 004).

## Repository layout
- `backend/` — Spring Boot 4 API. Has its own CLAUDE.md.
- `frontend/` — Angular (latest major) app. Has its own CLAUDE.md.
- `docs/product-vision.md` — product scope and roadmap.
- `docs/decisions.md` — decision log. Read the relevant entry before changing anything
  it covers.

## Constraints that must not be broken
- No copyrighted lyrics or tabs anywhere in the repository, including tests and
  fixtures. Use public-domain, original or clearly invented content (decision 001).
- Chord sheets are stored as a structured model (song → sections → lines → segments;
  chords relative to the song's key). ChordPro is an input format only, parsed in the
  backend (decisions 003, 007).
- The frontend never parses ChordPro; it renders and transposes the structured model
  (decisions 005, 007).
- Phase 1 has no authentication, accounts, submissions or requests (decision 004).
  Do not add them.

## Working agreement
- For non-trivial changes, propose a plan first and wait for approval.
- Keep each change small and focused on one task; a vertical slice (API, contract, UI)
  is one change.
- If a task conflicts with a decision in docs/decisions.md, stop and say so instead of
  working around it.
- New decisions are appended to docs/decisions.md in the existing format. Past entries
  are amended (status note plus corrected text), never silently rewritten.

## Conventions
- English everywhere: code, comments, commit messages, documentation.
- Commits follow Conventional Commits (`feat:`, `fix:`, `docs:`, `test:`, `refactor:`,
  `chore:`), with an area scope when useful, e.g. `feat(backend): ...`.
- Line endings are LF, enforced by .gitattributes. Development happens on Windows.

## Commands
To be added once backend and frontend are scaffolded.
