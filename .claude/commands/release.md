# /release — Publish Dogedex to Google Play

Publish a new production release of Dogedex to Google Play Store.

## What this command does

1. Verifies the workspace is clean and on `main`
2. Asks for (or accepts) the new `versionName`
3. Auto-increments `versionCode` in `app/build.gradle`
4. Optionally updates `app/src/main/play/release-notes/en-US/default.txt`
5. Runs `./gradlew :app:publishReleaseBundle` (builds signed AAB + uploads to Play Store)
6. On success: commits the version bump, creates a git tag, pushes to `origin/main`
7. On failure: reverts `build.gradle` automatically

## Usage

```
/release              # prompts for versionName interactively
/release 1.1.0        # sets versionName to 1.1.0 non-interactively
```

## Prerequisites

Before running, make sure:
- `play-service-account.json` exists in the project root (or path is set via `PLAY_SERVICE_ACCOUNT_JSON` in `local.properties`)
- The signing keystore credentials are in `local.properties`
- The working tree is clean (`git status` is empty)

## Instructions for Claude

When the user says "release", "publish to Play Store", "deploy to production", or similar:

1. Ask for the new version name if not provided (show current version from `app/build.gradle`)
2. Ask if they want to update the release notes
3. Run: `./scripts/release.sh <versionName>`
4. Report the outcome — tag created, Play Store track updated, commit pushed
