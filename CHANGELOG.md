# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Added
- Initial project scaffolding.
- F-16: Display-name input during pairing flow — user enters their name before joining or creating a channel. Empty mandatory field with placeholder text (no pre-fill, no `Build.MODEL`); "Continue" button disabled until at least one visible character is entered. Maximum 32 Unicode codepoints. Persisted in DataStore via `:core:identity` (`IdentityRepository`), editable via Settings → Channel → "Your Name". Propagated reactively to mDNS TXT `dname` (debounced 300 ms) and UDP broadcast beacon (next 3 s tick). Corruption fallback: `Peer-{first8hex(pk)}` — never `Build.MODEL` or device hostname. Inbound `dname` values from other peers are sanitized in `:service:discovery` (NFC normalization, Bidi-override stripping, combining-mark limit, codepoint truncation) before being shown in the UI; see security finding F-PRIV-04.
