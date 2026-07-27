#!/bin/bash
set -euo pipefail

# Script to calculate next version and tag the current commit
# Reads version from build.gradle, increments it, and tags the current commit

# Get current version from build.gradle
CURRENT_VERSION=$(grep "^version = " build.gradle | awk -F"'" '{print $2}')
echo "Current version in build.gradle: $CURRENT_VERSION"

# Parse version components
IFS='.' read -r MAJOR MINOR PATCH <<< "$CURRENT_VERSION"

# Calculate next patch version
PATCH=$((PATCH + 1))
NEXT_VERSION="$MAJOR.$MINOR.$PATCH"
echo "Next version to tag: $NEXT_VERSION"

# Tag current commit with next version
TAG="v$NEXT_VERSION"
git tag -a "$TAG" -m "Release $NEXT_VERSION"
git push origin "$TAG"
echo "Created and pushed tag: $TAG"



