#!/bin/sh

# Gradle wrapper — uses the pre-installed Gradle on GitHub Actions runners
# Falls back to downloading if not available

DIR="$(cd "$(dirname "$0")" && pwd)"

# Try using system-installed Gradle first
if command -v gradle >/dev/null 2>&1; then
    exec gradle "$@"
fi

# Try using Gradle from Gradle home
if [ -d "$HOME/.gradle" ]; then
    GRADLE_FOUND=$(find "$HOME/.gradle" -name "gradle" -type f -path "*/bin/*" 2>/dev/null | head -1)
    if [ -n "$GRADLE_FOUND" ]; then
        exec "$GRADLE_FOUND" "$@"
    fi
fi

# Download Gradle
GRADLE_VERSION="8.2"
GRADLE_URL="https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"
GRADLE_CACHE="/tmp/gradle-${GRADLE_VERSION}"

if [ ! -d "$GRADLE_CACHE" ]; then
    echo "Downloading Gradle ${GRADLE_VERSION}..."
    curl -fsSL -o "/tmp/gradle.zip" "$GRADLE_URL"
    mkdir -p "$GRADLE_CACHE"
    unzip -q -o "/tmp/gradle.zip" -d "/tmp/"
    mv "/tmp/gradle-${GRADLE_VERSION}" "$GRADLE_CACHE"
fi

exec "$GRADLE_CACHE/bin/gradle" "$@"
