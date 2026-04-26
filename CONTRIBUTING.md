# Contributing to HeraTalk

Thank you for your interest in contributing.

## Prerequisites

- JDK 21 (Temurin)
- Android SDK 36 + NDK r28
- See [docs/devcontainer.md](docs/devcontainer.md) for the recommended dev container setup.

## Workflow

1. Fork the repository and create a feature branch from `main`.
2. Write code following the conventions in [CLAUDE.md](.claude/CLAUDE.md).
3. Add or update tests for every change.
4. Run `./gradlew check` locally before pushing.
5. Open a pull request against `main`; fill in the PR template.

## Code style

- Kotlin 2.3.x, explicit API mode (`-Xexplicit-api=strict`)
- Formatter: ktlint (enforced in CI via Spotless)
- Static analysis: detekt
- Line length: 120 characters, 4-space indentation
- Copyright header required on every `.kt` file (checked by Spotless)

## Commit messages

Follow [Conventional Commits](https://www.conventionalcommits.org/):
`feat:`, `fix:`, `docs:`, `chore:`, `refactor:`, `test:`, `ci:`, `build:`, `i18n:`

## Hard limits

The following are non-negotiable out-of-scope items — do not open PRs for them:
- Analytics, telemetry, or crash reporting with a cloud component
- Cloud backend or account system
- Weakening cryptography (downgrading ciphers, reducing key size)
- Third-party authentication
- Advertising

## Code of Conduct

See [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).
