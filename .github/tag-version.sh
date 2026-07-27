#!/bin/bash
set -euo pipefail

# Script to calculate next version and tag the current commit
# Uses build.gradle as the base version, finds the latest tag for that major.minor version,
# and increments the patch version

# Get base version from build.gradle
BUILD_VERSION=$(grep "^version = " build.gradle | awk -F"'" '{print $2}')
echo "Base version in build.gradle: $BUILD_VERSION"

# Parse version components
IFS='.' read -r MAJOR MINOR BASE_PATCH <<< "$BUILD_VERSION"

# Find latest tag matching this major.minor version (e.g., v1.2.*)
LATEST_MATCHING_TAG=$(git tag -l "v$MAJOR.$MINOR.*" | sort -V | tail -1)

if [ -z "$LATEST_MATCHING_TAG" ]; then
  # No tags exist for this major.minor version yet, use base version from build.gradle
  echo "No existing tags for v$MAJOR.$MINOR.*, starting from base version"
  NEXT_VERSION="$BUILD_VERSION"
else
  # Extract patch version from latest matching tag and increment it
  echo "Latest tag for v$MAJOR.$MINOR.*: $LATEST_MATCHING_TAG"
  LATEST_VERSION="${LATEST_MATCHING_TAG#v}"
  IFS='.' read -r _ _ LATEST_PATCH <<< "$LATEST_VERSION"
  NEXT_PATCH=$((LATEST_PATCH + 1))
  NEXT_VERSION="$MAJOR.$MINOR.$NEXT_PATCH"
fi

echo "Next version to tag: $NEXT_VERSION"

# Tag current commit with next version
TAG="v$NEXT_VERSION"
git tag -a "$TAG" -m "Release $NEXT_VERSION"
git push origin "$TAG"
echo "Created and pushed tag: $TAG"



