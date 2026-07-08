#!/usr/bin/env bash
# =============================================================================
# Dogedex — Release to Google Play
#
# Usage:
#   ./scripts/release.sh 1.1.0               # production track (default)
#   ./scripts/release.sh 1.1.0 --internal    # internal testing track
#   ./scripts/release.sh                     # prompts for versionName
#
# Prerequisites:
#   1. play-service-account.json in project root (or set PLAY_SERVICE_ACCOUNT_JSON
#      in local.properties with the absolute path).
#   2. Signing keys configured in local.properties.
#   3. Python deps installed once: pip3 install -r scripts/requirements.txt
# =============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$SCRIPT_DIR/.."
BUILD_GRADLE="$ROOT/app/build.gradle"
RELEASE_NOTES="$ROOT/app/src/main/play/release-notes/en-US/default.txt"
PACKAGE_NAME="com.espert.dogedex"
AAB_PATH="$ROOT/app/build/outputs/bundle/release/doggito-poio_release.aab"

# ── Parse flags ───────────────────────────────────────────────────────────────
TRACK="production"
POSITIONAL_ARGS=()
for arg in "$@"; do
  case "$arg" in
    --internal|-i) TRACK="internal" ;;
    *) POSITIONAL_ARGS+=("$arg") ;;
  esac
done
set -- "${POSITIONAL_ARGS[@]+"${POSITIONAL_ARGS[@]}"}"

# Resolve service account JSON path (local.properties override or project root default)
LOCAL_PROPS="$ROOT/local.properties"
if [[ -f "$LOCAL_PROPS" ]]; then
  CREDS_PATH=$(grep -E '^PLAY_SERVICE_ACCOUNT_JSON=' "$LOCAL_PROPS" \
               | cut -d'=' -f2- | tr -d '\r' || true)
fi
CREDS_PATH="${CREDS_PATH:-$ROOT/play-service-account.json}"

# ── 1. Guard: clean workspace on main ────────────────────────────────────────
cd "$ROOT"

CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)
if [[ "$CURRENT_BRANCH" != "main" ]]; then
  echo "❌  Must be on 'main' branch (currently on '$CURRENT_BRANCH')."
  exit 1
fi

if [[ -n $(git status --porcelain) ]]; then
  echo "❌  Working tree is dirty. Commit or stash your changes first."
  git status --short
  exit 1
fi

# ── 2. Verify service account exists ─────────────────────────────────────────
if [[ ! -f "$CREDS_PATH" ]]; then
  echo "❌  Service account JSON not found at: $CREDS_PATH"
  echo ""
  echo "    How to create one:"
  echo "    1. Play Console → Setup → API access → Link Google Cloud project"
  echo "    2. Google Cloud Console → IAM → Service Accounts → Create"
  echo "    3. Grant the service account 'Release Manager' in Play Console"
  echo "    4. Download the JSON key and save it as: play-service-account.json"
  echo "       (project root, it is already in .gitignore)"
  echo ""
  echo "    Or set PLAY_SERVICE_ACCOUNT_JSON=/absolute/path in local.properties"
  exit 1
fi

# ── 3. Verify Python deps ─────────────────────────────────────────────────────
if ! python3 -c "import googleapiclient, google.auth" 2>/dev/null; then
  echo "📦  Installing Python dependencies…"
  pip3 install -r "$SCRIPT_DIR/requirements.txt" --quiet
fi

# ── 4. Resolve versionName ────────────────────────────────────────────────────
CURRENT_VERSION_NAME=$(grep 'versionName' "$BUILD_GRADLE" | grep -oE '"[^"]+"' | tr -d '"')
if [[ $# -ge 1 ]]; then
  NEW_VERSION_NAME="$1"
else
  read -r -p "Enter versionName [current: $CURRENT_VERSION_NAME]: " NEW_VERSION_NAME
  NEW_VERSION_NAME="${NEW_VERSION_NAME:-$CURRENT_VERSION_NAME}"
fi

# ── 5. Bump versionCode ───────────────────────────────────────────────────────
CURRENT_VERSION_CODE=$(grep 'versionCode' "$BUILD_GRADLE" | grep -oE '[0-9]+')
NEW_VERSION_CODE=$(( CURRENT_VERSION_CODE + 1 ))

echo ""
echo "📦  Preparing release:"
echo "    versionCode  : $CURRENT_VERSION_CODE → $NEW_VERSION_CODE"
echo "    versionName  : $NEW_VERSION_NAME"
echo ""

# ── 6. Update release notes ───────────────────────────────────────────────────
echo "📝  Current release notes:"
cat "$RELEASE_NOTES"
echo ""
read -r -p "Update release notes? [y/N]: " UPDATE_NOTES
if [[ "$UPDATE_NOTES" =~ ^[Yy]$ ]]; then
  read -r -p "New release notes (max 500 chars): " NEW_NOTES
  echo "$NEW_NOTES" > "$RELEASE_NOTES"
  echo "✅  Release notes updated."
fi

# ── 7. Patch build.gradle ─────────────────────────────────────────────────────
sed -i '' \
  "s/versionCode $CURRENT_VERSION_CODE/versionCode $NEW_VERSION_CODE/" \
  "$BUILD_GRADLE"
sed -i '' \
  "s/versionName \"$CURRENT_VERSION_NAME\"/versionName \"$NEW_VERSION_NAME\"/" \
  "$BUILD_GRADLE"
echo "✅  build.gradle patched."

# ── 8. Build signed release bundle ───────────────────────────────────────────
echo ""
echo "🔨  Building release bundle…"
if ! ./gradlew :app:bundleRelease --no-daemon --quiet; then
  echo "❌  Build failed. Reverting build.gradle…"
  sed -i '' \
    "s/versionCode $NEW_VERSION_CODE/versionCode $CURRENT_VERSION_CODE/" \
    "$BUILD_GRADLE"
  sed -i '' \
    "s/versionName \"$NEW_VERSION_NAME\"/versionName \"$CURRENT_VERSION_NAME\"/" \
    "$BUILD_GRADLE"
  exit 1
fi
echo "✅  Bundle built: $AAB_PATH"

# ── 9. Upload to Google Play ──────────────────────────────────────────────────
echo ""
echo "🚀  Uploading to Google Play ($TRACK)…"
if ! python3 "$SCRIPT_DIR/upload_to_play.py" \
    --package "$PACKAGE_NAME" \
    --aab     "$AAB_PATH" \
    --creds   "$CREDS_PATH" \
    --track   "$TRACK" \
    --notes   "$RELEASE_NOTES"; then
  echo ""
  echo "❌  Upload failed. Reverting build.gradle…"
  sed -i '' \
    "s/versionCode $NEW_VERSION_CODE/versionCode $CURRENT_VERSION_CODE/" \
    "$BUILD_GRADLE"
  sed -i '' \
    "s/versionName \"$NEW_VERSION_NAME\"/versionName \"$CURRENT_VERSION_NAME\"/" \
    "$BUILD_GRADLE"
  exit 1
fi

# ── 10. Commit, tag and push ──────────────────────────────────────────────────
TAG="v$NEW_VERSION_NAME"

git add app/build.gradle app/src/main/play/release-notes/
git commit -m "chore(release): bump version to $NEW_VERSION_NAME ($NEW_VERSION_CODE)

- versionCode: $CURRENT_VERSION_CODE → $NEW_VERSION_CODE
- versionName: $NEW_VERSION_NAME
- Published to Google Play $TRACK track

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>"

git tag -a "$TAG" -m "Release $NEW_VERSION_NAME"
git push origin main --tags

echo ""
echo "🎉  $NEW_VERSION_NAME ($NEW_VERSION_CODE) uploaded to Google Play ($TRACK track)!"
echo "    Tag $TAG pushed to origin/main"
