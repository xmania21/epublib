# Changelog

All notable changes to the epublib project will be documented in this file.

---

## [5.0.0-SNAPSHOT] - 2026-08-08

### Added
- **Fluent Builder API**: Added `Book.builder()` fluent API matching typical builder pattern structures.
- **EpubStreamReader & EpubStreamWriter**: New streaming reader and writer classes for memory-efficient processing of extremely large EPUB books (avoids loading whole files to memory).
- **PicoCLI CLI**: Modern subcommand structure (`read`, `write`, `validate`, `convert`) replacing legacy flat argument list.
- **EpubValidator**: Core structural validation engine reporting spec warnings and errors using `ValidationReport`.
- **ValidationIssue**: Structured representation of specific validation problems.
- **Custom Exceptions**: Unchecked exception hierarchy (`EpublibException` -> `EpubReadException`/`EpubWriteException`/`EpubValidationException`) with resource context.
- **New tests**: Comprehensive suite covering roundtrips, builders, validation, and streaming.

### Changed
- **Maven compilation**: Bumped compiler and plugin compatibility, target Java 25.
- **Dependency upgrade**:
  - `commons-io` to `2.16.1`
  - `commons-lang3` to `3.14.0` (introduced alongside legacy)
  - `jsoup` to `1.18.1` (transitional alongside legacy)
  - `commons-vfs2` to `2.9.0` (replaced commons-vfs 1.0)
  - `jackson-databind` 2.17.1 (for JSON support)
  - `picocli` 4.7.6 (for CLI parsing)

### Fixed
- Fixed hardcoded distributionManagement deployment path.
- Defunct Travis CI setup replaced with modern GitHub Actions CI.
- Replaced swallowed/silent standard output exceptions with structured exception throws.
