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
/release              # prompts for versionName interactively → production track
/release 1.1.0        # sets versionName to 1.1.0 → production track
/release 1.1.0 --internal   # uploads to internal testing track instead
```

## Tracks

| Flag | Play Store track | When to use |
|---|---|---|
| _(none)_ | `production` | Full public release |
| `--internal` / `-i` | `internal` | Testing with a small group before production |

## Prerequisites

Before running, make sure:
- `play-service-account.json` exists in the project root (or path is set via `PLAY_SERVICE_ACCOUNT_JSON` in `local.properties`)
- The signing keystore credentials are in `local.properties`
- The working tree is clean (`git status` is empty)
- Python deps installed once: `pip3 install -r scripts/requirements.txt`

## Release notes

Release notes live in `app/src/main/play/release-notes/<locale>/default.txt`.
All locale folders are auto-discovered and uploaded. Max 500 chars per locale.

Supported locales currently: `en-US`, `es-ES`.

## Instructions for Claude

When the user says "release", "publish", "deploy", "push to internal testing", or similar:

1. Ask for the new version name if not provided (show current from `app/build.gradle`)
2. Ask which track: internal testing or production? Default to internal if they mention testing.
3. Ask if they want to update the release notes (show current content)
4. Run: `./scripts/release.sh <versionName> [--internal]`
5. Report the outcome — tag created, Play Store track updated, commit pushed
