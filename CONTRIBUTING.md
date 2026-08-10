# Contributing to epublib

Thank you for your interest in contributing to epublib! Below are guidelines to help you get started.

---

## Development Setup

To build and test the project locally, you need JDK 25 and Maven 3.9+.

```bash
# Clone the repository
git clone https://github.com/psiegman/epublib.git
cd epublib

# Build and run all tests
mvn clean verify
```

---

## Code Style & Constraints

We maintain a strict quality bar to ensure the library remains lightweight and highly compatible:

1. **Android Compatibility**:
   - `epublib-core` must remain compatible with Android API levels below 34.
   - Do **NOT** use Java `record` classes or `sealed` types in `epublib-core`.
   - Use standard Java classes, builders, and traditional interfaces for all domain types.
2. **Quality Gates**:
   - All tests must pass cleanly.
   - **JaCoCo** test coverage threshold is set to **80%** minimum.
   - **SpotBugs** must report zero high/medium severity bugs.
   - **Checkstyle** rules apply. Run `mvn checkstyle:check` before submitting PRs.

---

## Pull Request Process

1. Fork the repository and create your branch from `main`.
2. Add comprehensive unit tests for any new features or bug fixes.
3. Ensure the build passes completely (`mvn clean verify`).
4. Write clear, descriptive commit messages describing the *intent* of the changes.
