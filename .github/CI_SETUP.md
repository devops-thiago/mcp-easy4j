# CI/CD Setup for MCP Easy4J

## Overview

This document describes the CI/CD setup for the MCP Easy4J project on GitHub.

## GitHub Actions Workflows

### 1. CI Workflow (`.github/workflows/ci.yaml`)

Runs on every push and pull request to `main` and `develop` branches.

**Steps:**
1. **Checkout code** - Fetches the repository with full history
2. **Set up JDK 21** - Configures Java 21 (Temurin distribution)
3. **Cache dependencies** - Caches SonarCloud and Maven packages
4. **Check code style** - Runs Checkstyle (non-blocking)
5. **Check code format** - Validates code formatting (non-blocking)
6. **Compile** - Compiles the project
7. **Run SpotBugs** - Static analysis (non-blocking)
8. **Run tests** - Executes JUnit tests (non-blocking)
9. **Verify coverage** - Checks JaCoCo coverage thresholds (non-blocking)
10. **Build package** - Creates JAR file
11. **SonarCloud analysis** - Uploads code quality metrics (main branch only)
12. **Upload to Codecov** - Uploads coverage reports
13. **Upload artifacts** - Saves JaCoCo reports and test results

### 2. Deploy Workflow (`.github/workflows/deploy.yaml`)

Handles deployment to Maven Central.

**Two deployment modes:**

#### Snapshot Deployment
- Triggers on push to `main` branch (non-tag)
- Appends commit hash to version (e.g., `1.0.0-abc123-SNAPSHOT`)
- Deploys to Maven Central snapshots repository

#### Release Deployment
- Triggers on version tags (e.g., `v1.0.0`)
- Uses tag version for artifact
- Signs artifacts with GPG
- Deploys to Maven Central
- Creates GitHub release with artifacts

## Required GitHub Secrets

Configure these secrets in your GitHub repository settings:

### SonarCloud
- `SONAR_TOKEN` - SonarCloud authentication token
  - Get from: https://sonarcloud.io/account/security

### Codecov
- `CODECOV_TOKEN` - Codecov upload token
  - Get from: https://codecov.io/gh/devops-thiago/mcp-easy4j/settings

### Maven Central (OSSRH)
- `OSSRH_USERNAME` - Sonatype OSSRH username
- `OSSRH_TOKEN` - Sonatype OSSRH token
  - Get from: https://central.sonatype.com/account

### GPG Signing
- `GPG_PRIVATE_KEY` - GPG private key for signing artifacts
- `GPG_PRIVATE_KEY_PASSPHRASE` - Passphrase for GPG key

## Setting Up External Services

### 1. SonarCloud Setup

1. Go to https://sonarcloud.io
2. Sign in with GitHub
3. Import the `devops-thiago/mcp-easy4j` repository
4. Project key: `devops-thiago_mcp-easy4j`
5. Organization: `devops-thiago`
6. Generate a token and add it as `SONAR_TOKEN` secret

### 2. Codecov Setup

1. Go to https://codecov.io
2. Sign in with GitHub
3. Add the `devops-thiago/mcp-easy4j` repository
4. Copy the upload token
5. Add it as `CODECOV_TOKEN` secret in GitHub

### 3. Maven Central Setup

1. Create account at https://central.sonatype.com
2. Verify domain ownership for `io.github.mcpeasy4j`
3. Generate user token
4. Add credentials as `OSSRH_USERNAME` and `OSSRH_TOKEN` secrets

### 4. GPG Key Setup

Generate GPG key for signing:

```bash
# Generate key
gpg --full-generate-key

# Export private key
gpg --armor --export-secret-keys YOUR_KEY_ID > private-key.asc

# Add to GitHub secrets:
# - GPG_PRIVATE_KEY: Contents of private-key.asc
# - GPG_PRIVATE_KEY_PASSPHRASE: Your passphrase
```

## Maven Configuration

### Code Quality Plugins

The `pom.xml` includes:

- **JaCoCo** - Code coverage (70% line, 60% branch minimum)
- **Checkstyle** - Code style checks (Google style)
- **SpotBugs** - Static analysis
- **Formatter** - Code formatting validation
- **Surefire** - Test execution

### Release Profile

Activated with `-P release`:

- Generates source JAR
- Generates Javadoc JAR
- Signs artifacts with GPG
- Publishes to Maven Central

## Code Quality Thresholds

### JaCoCo Coverage
- Line coverage: 70%
- Branch coverage: 60%

### Checkstyle
- Configuration: Google Java Style Guide
- Severity: Warning (non-blocking)

### SpotBugs
- Effort: Max
- Threshold: Low
- Non-blocking

## Badges

The README includes badges for:

- CI build status
- Code coverage (Codecov)
- Quality gate (SonarCloud)
- Maven Central version
- License

## Deployment Process

### Snapshot Deployment

```bash
# Automatic on push to main
git push origin main
```

### Release Deployment

```bash
# Create and push a version tag
git tag v1.0.0
git push origin v1.0.0
```

## Local Testing

Test the build locally before pushing:

```bash
# Run all checks
mvn clean verify

# Run with coverage
mvn clean test jacoco:report

# Check coverage thresholds
mvn jacoco:check

# Run code style checks
mvn checkstyle:check

# Run SpotBugs
mvn spotbugs:check

# Format code
mvn formatter:format

# Build release artifacts
mvn clean package -P release
```

## Troubleshooting

### Coverage Upload Fails
- Verify `CODECOV_TOKEN` is set correctly
- Check that `target/site/jacoco/jacoco.xml` exists
- Review Codecov logs in GitHub Actions

### SonarCloud Analysis Fails
- Verify `SONAR_TOKEN` is set correctly
- Check project key matches: `devops-thiago_mcp-easy4j`
- Ensure organization is correct: `devops-thiago`

### Maven Central Deployment Fails
- Verify OSSRH credentials are correct
- Check GPG key is properly configured
- Ensure version follows semantic versioning
- Review deployment logs for specific errors

## Continuous Improvement

The CI configuration uses `continue-on-error: true` for quality checks to allow the build to proceed while establishing baselines. As the project matures:

1. Remove `continue-on-error` from test execution
2. Increase coverage thresholds
3. Make Checkstyle and SpotBugs blocking
4. Add additional quality gates

## References

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [SonarCloud Documentation](https://docs.sonarcloud.io/)
- [Codecov Documentation](https://docs.codecov.com/)
- [Maven Central Publishing](https://central.sonatype.org/publish/)
- [JaCoCo Documentation](https://www.jacoco.org/jacoco/trunk/doc/)
