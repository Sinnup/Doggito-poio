#!/usr/bin/env python3
"""
upload_to_play.py — Upload an Android App Bundle to Google Play Store.

Requirements (install once):
  pip3 install google-api-python-client google-auth

Usage:
  python3 scripts/upload_to_play.py \
      --package  com.espert.dogedex \
      --aab      app/build/outputs/bundle/release/doggito-poio_release.aab \
      --creds    play-service-account.json \
      --track    production \
      --notes    app/src/main/play/release-notes/en-US/default.txt
"""

import argparse
import sys
from pathlib import Path

# ── Dependency check ─────────────────────────────────────────────────────────
try:
    from google.oauth2 import service_account
    from googleapiclient.discovery import build
    from googleapiclient.http import MediaFileUpload
except ImportError:
    print("❌  Missing dependencies. Run:")
    print("    pip3 install google-api-python-client google-auth")
    sys.exit(1)

SCOPES = ["https://www.googleapis.com/auth/androidpublisher"]


def upload(package_name: str, aab_path: str, creds_path: str, track: str, notes_path: str):
    # ── Validate inputs ───────────────────────────────────────────────────────
    if not Path(aab_path).exists():
        print(f"❌  AAB not found: {aab_path}")
        sys.exit(1)
    if not Path(creds_path).exists():
        print(f"❌  Service account JSON not found: {creds_path}")
        print("    See README.md for how to create one.")
        sys.exit(1)

    # ── Authenticate ──────────────────────────────────────────────────────────
    credentials = service_account.Credentials.from_service_account_file(
        creds_path, scopes=SCOPES
    )
    service = build("androidpublisher", "v3", credentials=credentials)
    edits = service.edits()

    # ── 1. Open an edit ───────────────────────────────────────────────────────
    edit = edits.insert(packageName=package_name).execute()
    edit_id = edit["id"]
    print(f"📝  Opened edit {edit_id}")

    try:
        # ── 2. Upload the bundle ──────────────────────────────────────────────
        print(f"⬆️   Uploading {Path(aab_path).name} …")
        media = MediaFileUpload(
            aab_path,
            mimetype="application/octet-stream",
            resumable=True,
        )
        bundle = (
            edits.bundles()
            .upload(packageName=package_name, editId=edit_id, media_body=media)
            .execute()
        )
        version_code = bundle["versionCode"]
        print(f"✅  Bundle uploaded  (versionCode {version_code})")

        # ── 3. Build release notes payload ────────────────────────────────────
        release_notes = []
        notes_file = Path(notes_path)
        if notes_file.exists():
            text = notes_file.read_text().strip()[:500]
            release_notes = [{"language": "en-US", "text": text}]

        # ── 4. Assign the bundle to the target track ──────────────────────────
        track_body = {
            "releases": [
                {
                    "versionCodes": [str(version_code)],
                    "releaseNotes": release_notes,
                    "status": "completed",
                }
            ]
        }
        edits.tracks().update(
            packageName=package_name,
            editId=edit_id,
            track=track,
            body=track_body,
        ).execute()
        print(f"🎯  Track '{track}' updated with versionCode {version_code}")

        # ── 5. Commit ─────────────────────────────────────────────────────────
        edits.commit(packageName=package_name, editId=edit_id).execute()
        print(f"🎉  Published to '{track}' track!")

    except Exception as exc:
        # Clean up the dangling edit so it doesn't block future publishes
        try:
            edits.delete(packageName=package_name, editId=edit_id).execute()
            print(f"🧹  Edit {edit_id} deleted after failure.")
        except Exception:
            pass
        print(f"❌  Publish failed: {exc}")
        sys.exit(1)


def main():
    parser = argparse.ArgumentParser(
        description="Upload an Android App Bundle to Google Play Store."
    )
    parser.add_argument("--package", required=True, help="Application ID (e.g. com.example.app)")
    parser.add_argument("--aab",     required=True, help="Path to the signed .aab file")
    parser.add_argument("--creds",   required=True, help="Path to the service account JSON key")
    parser.add_argument("--track",   default="production", help="Play Store track (default: production)")
    parser.add_argument(
        "--notes",
        default="app/src/main/play/release-notes/en-US/default.txt",
        help="Path to release notes .txt file (max 500 chars)",
    )
    args = parser.parse_args()
    upload(args.package, args.aab, args.creds, args.track, args.notes)


if __name__ == "__main__":
    main()
