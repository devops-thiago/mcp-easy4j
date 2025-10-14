# CI/CD Setup Guide

This document explains how to configure the CI/CD pipeline for the JSON-RPC library.

## Required Secrets

The following secrets need to be configured in your GitHub repository settings:

### 1. SONAR_TOKEN

**Purpose**: Authenticate with SonarCloud for code quality analysis.

**Setup Steps**:
1. Go to [SonarCloud](https://sonarcloud.io/)
2. Log in with your GitHub account
3. Create a new organization or use an existing one
4. Import your repository
5. Go to **My Account** → **Security** → **Generate Tokens**
6. Create a token with a descriptive name (e.g., "GitHub Actions")
7. Copy the token
8. In your GitHub repository, go to **Settings** → **Secrets and variables** → **Actions**
9. Click **New repository secret**
10. Name: `SONAR_TOKEN`
11. Value: Paste the token from SonarCloud
12. Click **Add secret**

### 2. CODECOV_TOKEN

**Purpose**: Upload coverage reports to Codecov.

**Setup Steps**:
1. Go to [Codecov](https://codecov.io/)
2. Log in with your GitHub account
3. Add your repository
4. Copy the upload token from the repository settings
5. In your GitHub repository, go to **Settings** → **Secrets and variables** → **Actions**
6. Click **New repository secret**
7. Name: `CODECOV_TOKEN`
8. Value: Paste the token from Codecov
9. Click **Add secret**

## SonarCloud Configuration

The project is already configured with:

```xml
<sonar.organization>devops-thiago</sonar.organization>
<sonar.projectKey>devops-thiago_json-rpc</sonar.projectKey>
```

And in `.github/workflows/ci.yaml`:

```yaml
-Dsonar.projectKey=devops-thiago_json-rpc \
-Dsonar.organization=devops-thiago \
```

If you need to change these, update both `pom.xml` and `.github/workflows/ci.yaml`.

## Running Checks Locally

### Check Style
```bash
mvn checkstyle:check
```

### Format Check
```bash
mvn formatter:validate
```

### Format Code
```bash
mvn formatter:format
```

### SpotBugs
```bash
mvn spotbugs:check
```

### Coverage
```bash
mvn clean test jacoco:report
mvn jacoco:check
```

### All Quality Checks
```bash
mvn clean verify
```

## Badges

Add these badges to your README.md (update the URLs with your repository):

```markdown
[![CI](https://github.com/devops-thiago/json-rpc/actions/workflows/ci.yaml/badge.svg)](https://github.com/devops-thiago/json-rpc/actions/workflows/ci.yaml)
[![codecov](https://codecov.io/gh/devops-thiago/json-rpc/branch/main/graph/badge.svg)](https://codecov.io/gh/devops-thiago/json-rpc)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=devops-thiago_json-rpc&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=devops-thiago_json-rpc)
[![Maven Central](https://img.shields.io/maven-central/v/br.com.arquivolivre/json-rpc.svg)](https://central.sonatype.com/artifact/br.com.arquivolivre/json-rpc)
```

## Troubleshooting

### Checkstyle Failures
- Review the Google Java Style Guide
- Run `mvn checkstyle:check` locally to see violations
- Common issues: line length, indentation, Javadoc

### Format Failures
- Run `mvn formatter:format` to auto-format code
- Commit the formatted code

### SpotBugs Warnings
- Review the SpotBugs report in `target/spotbugsXml.xml`
- Fix high-priority bugs first
- Use `@SuppressFBWarnings` annotation for false positives

### Coverage Below 80%
- Add more unit tests
- Focus on uncovered branches
- Review JaCoCo report at `target/site/jacoco/index.html`

### SonarCloud Issues
- Verify `SONAR_TOKEN` is set correctly
- Check organization and project key match
- Review SonarCloud dashboard for specific issues

### Codecov Upload Failures
- Verify `CODECOV_TOKEN` is set correctly
- Ensure JaCoCo report exists at `target/site/jacoco/jacoco.xml`
- Check Codecov dashboard for upload status

## Maven Central Deployment

### Required Secrets for Deployment

#### 3. OSSRH_USERNAME and OSSRH_TOKEN

**Purpose**: Authenticate with Sonatype OSSRH (Maven Central).

**Setup Steps**:
1. Create an account at [Sonatype JIRA](https://issues.sonatype.org/)
2. Create a ticket to claim your groupId (e.g., `br.com.arquivolivre`)
3. Once approved, generate a user token:
   - Go to your profile
   - Click "User Token"
   - Generate a new token
4. Add secrets to GitHub:
   - `OSSRH_USERNAME`: Your Sonatype username or token username
   - `OSSRH_TOKEN`: Your Sonatype password or token password

#### 4. GPG_PRIVATE_KEY and GPG_PASSPHRASE

**Purpose**: Sign artifacts for Maven Central.

**Setup Steps**:
1. Generate a GPG key pair:
   ```bash
   gpg --gen-key
   ```
2. List your keys:
   ```bash
   gpg --list-secret-keys --keyid-format LONG
   ```
3. Export your private key:
   ```bash
   gpg --export-secret-keys -a YOUR_KEY_ID > private-key.asc
   ```
4. Add secrets to GitHub:
   - `GPG_PRIVATE_KEY`: Content of `private-key.asc`
   - `GPG_PASSPHRASE`: The passphrase you used when creating the key
5. Publish your public key:
   ```bash
   gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
   ```

### Deployment Workflows

**Snapshot Deployment** (automatic on push to main):
- Triggered when code is pushed to `main` branch
- Version is automatically set to `X.Y.Z-SNAPSHOT`
- Deployed to Maven Central snapshots repository

**Release Deployment** (automatic on tag):
- Triggered when a tag starting with `v` is pushed (e.g., `v1.0.0`)
- Version is extracted from the tag
- Deployed to Maven Central releases repository
- Creates a GitHub release with artifacts

### Creating a Release

1. Update version in `pom.xml` if needed
2. Commit and push changes
3. Create and push a tag:
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```
4. The workflow will automatically:
   - Build and test
   - Sign artifacts
   - Deploy to Maven Central
   - Create GitHub release

### Using Snapshots

To use snapshot versions in your project:

```xml
<repositories>
    <repository>
        <id>ossrh-snapshots</id>
        <url>https://s01.oss.sonatype.org/content/repositories/snapshots</url>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>

<dependency>
    <groupId>br.com.arquivolivre</groupId>
    <artifactId>json-rpc</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```
