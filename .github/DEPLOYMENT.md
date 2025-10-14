# Deployment Guide

## Overview

This project uses GitHub Actions to automatically deploy to Maven Central:

- **Snapshots**: Automatically deployed when code is pushed to `main` branch
- **Releases**: Automatically deployed when a version tag is pushed

## Prerequisites

Ensure all required secrets are configured in GitHub repository settings:

- `OSSRH_USERNAME` - Sonatype OSSRH username
- `OSSRH_TOKEN` - Sonatype OSSRH token
- `GPG_PRIVATE_KEY` - GPG private key for signing
- `GPG_PASSPHRASE` - GPG key passphrase

See [SETUP.md](SETUP.md) for detailed instructions on obtaining these credentials.

## Snapshot Deployment

Snapshots are automatically deployed on every push to `main`:

```bash
git checkout main
git pull
# Make your changes
git add .
git commit -m "Your changes"
git push origin main
```

The workflow will:
1. Run all CI checks
2. Append `-SNAPSHOT` to the version
3. Deploy to Maven Central snapshots repository

Snapshot artifacts are available at:
```
https://s01.oss.sonatype.org/content/repositories/snapshots/br/com/arquivolivre/json-rpc/
```

## Release Deployment

### Step 1: Prepare the Release

1. Ensure `main` branch is up to date:
   ```bash
   git checkout main
   git pull
   ```

2. Update version in `pom.xml` (remove `-SNAPSHOT` if present):
   ```xml
   <version>1.0.0</version>
   ```

3. Update `CHANGELOG.md` with release notes (if you have one)

4. Commit the version change:
   ```bash
   git add pom.xml
   git commit -m "Prepare release 1.0.0"
   git push origin main
   ```

### Step 2: Create and Push Tag

1. Create an annotated tag:
   ```bash
   git tag -a v1.0.0 -m "Release version 1.0.0"
   ```

2. Push the tag:
   ```bash
   git push origin v1.0.0
   ```

### Step 3: Monitor Deployment

1. Go to GitHub Actions tab
2. Watch the "Deploy to Maven Central" workflow
3. Verify the deployment succeeds

### Step 4: Verify Release

1. Check Maven Central (may take 10-30 minutes):
   ```
   https://central.sonatype.com/artifact/br.com.arquivolivre/json-rpc
   ```

2. Check GitHub Releases:
   ```
   https://github.com/devops-thiago/json-rpc/releases
   ```

### Step 5: Prepare for Next Development

1. Update version to next SNAPSHOT:
   ```xml
   <version>1.1.0-SNAPSHOT</version>
   ```

2. Commit and push:
   ```bash
   git add pom.xml
   git commit -m "Prepare for next development iteration"
   git push origin main
   ```

## Version Numbering

Follow [Semantic Versioning](https://semver.org/):

- **MAJOR** version: Incompatible API changes
- **MINOR** version: Backwards-compatible functionality
- **PATCH** version: Backwards-compatible bug fixes

Examples:
- `1.0.0` - Initial release
- `1.1.0` - New features, backwards compatible
- `1.1.1` - Bug fixes
- `2.0.0` - Breaking changes

## Rollback a Release

If you need to rollback a release:

1. Delete the tag locally and remotely:
   ```bash
   git tag -d v1.0.0
   git push origin :refs/tags/v1.0.0
   ```

2. Delete the GitHub release (if created)

3. Contact Sonatype support to drop the release from Maven Central (if already synced)

## Troubleshooting

### Deployment Fails with GPG Error

- Verify `GPG_PRIVATE_KEY` is correctly formatted
- Ensure `GPG_PASSPHRASE` matches the key
- Check that the public key is published to a keyserver

### Deployment Fails with Authentication Error

- Verify `OSSRH_USERNAME` and `OSSRH_TOKEN` are correct
- Ensure your Sonatype account has permissions for the groupId
- Check that your JIRA ticket for groupId claim is approved

### Release Not Appearing on Maven Central

- Check Sonatype Nexus Repository Manager
- Verify the staging repository was released
- Wait 10-30 minutes for sync to Maven Central
- Check for any validation errors in Nexus

### Tag Already Exists

If you need to recreate a tag:
```bash
git tag -d v1.0.0
git push origin :refs/tags/v1.0.0
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

## Manual Deployment

If you need to deploy manually:

```bash
# For snapshot
mvn clean deploy -P release

# For release (ensure version doesn't end with -SNAPSHOT)
mvn versions:set -DnewVersion=1.0.0
mvn clean deploy -P release
```

Note: Manual deployment requires GPG and Maven settings configured locally.
