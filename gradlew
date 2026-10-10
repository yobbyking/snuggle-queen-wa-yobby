#!/bin/sh
DIR="$(cd "$(dirname "$0")" && pwd)"
GRADLE_VERSION="8.7"
GRADLE_CACHE="/tmp/gradle-${GRADLE_VERSION}"
if [ ! -d "$GRADLE_CACHE" ]; then
    echo "Downloading Gradle ${GRADLE_VERSION}..."
    curl -fsSL -o "/tmp/gradle.zip" "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"
    unzip -q -o "/tmp/gradle.zip" -d "/tmp/"
    mv "/tmp/gradle-${GRADLE_VERSION}" "$GRADLE_CACHE"
fi
exec "$GRADLE_CACHE/bin/gradle" "$@"
