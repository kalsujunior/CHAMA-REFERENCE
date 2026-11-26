#!/usr/bin/env bash
set -euo pipefail

repo_root="$(git rev-parse --show-toplevel)"
expected_blob_sha1="ad9091b7ea61919de0bab0dd0921b7a164982f92"

# This is the Git blob SHA-1 recorded for the user's original Chama portrait.
# It is deliberately not a checksum of a generated, cropped, or edited image.
for relative_path in \
  "shama.png" \
  "RELIVE/app/src/main/res/drawable-nodpi/chama_portrait.png"; do
  file_path="$repo_root/$relative_path"
  if [[ ! -f "$file_path" ]]; then
    echo "ERROR: portrait file is missing: $relative_path" >&2
    exit 1
  fi

  actual_blob_sha1="$(git -C "$repo_root" hash-object "$file_path")"
  if [[ "$actual_blob_sha1" != "$expected_blob_sha1" ]]; then
    echo "ERROR: protected Chama portrait was changed: $relative_path" >&2
    echo "Expected Git blob SHA-1: $expected_blob_sha1" >&2
    echo "Actual Git blob SHA-1:   $actual_blob_sha1" >&2
    exit 1
  fi
done

# Keep the machine-readable identity lock aligned with the immutable portrait.
python3 - "$repo_root/RELIVE/app/src/main/assets/chama/rig/chama-rig-manifest.json" <<'PY'
import json
import sys

manifest_path = sys.argv[1]
with open(manifest_path, "r", encoding="utf-8") as source:
    manifest = json.load(source)

identity = manifest.get("identityPolicy", {})
required_true = (
    "originalPortraitIsImmutable",
    "portraitPixelsMustRemainIdentical",
    "portraitMayNotBeCroppedRetouchedWarpedReconstructedOrRepainted",
    "portraitMustBeRenderedAsIndependentStaticOverlay",
    "noHeadRotation",
    "noEyeMouthJawFacialAnimation",
    "noLipSync",
)
overlay = manifest.get("overlayContract", {})
required_false = (
    "allowTransform",
    "allowCrop",
    "allowColorAdjustment",
    "allowFaceDetectionDrivenEdits",
)

errors = [f"identityPolicy.{key} must be true"
          for key in required_true if identity.get(key) is not True]
errors += [f"overlayContract.{key} must be false"
           for key in required_false if overlay.get(key) is not False]
if overlay.get("bodyMustAnimateBehindOrBesidePortrait") is not True:
    errors.append("overlayContract.bodyMustAnimateBehindOrBesidePortrait must be true")

if errors:
    for error in errors:
        print("ERROR:", error, file=sys.stderr)
    raise SystemExit(1)

print("Chama identity lock: OK")
PY

echo "Chama portrait integrity: OK (original image unchanged)."
