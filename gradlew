#!/bin/sh
DIR="$(cd "$(dirname "$0")" && pwd)"
GRADLE_VERSION="8.7"
GRADLE_CACHE="/tmp/gradle-${GRADLE_VERSION}"
if [ ! -f "$GRADLE_CACHE/bin/gradle" ]; then
    echo "Downloading Gradle ${GRADLE_VERSION}..."
    rm -rf "$GRADLE_CACHE"
    curl -fsSL -o "/tmp/gradle-${GRADLE_VERSION}.zip" "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"
    unzip -q -o "/tmp/gradle-${GRADLE_VERSION}.zip" -d "/tmp/"
fi
exec "/tmp/gradle-${GRADLE_VERSION}/bin/gradle" "$@"
