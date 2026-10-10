#!/bin/sh

# Gradle wrapper script
# Downloads and runs the correct Gradle version

DIR="$(cd "$(dirname "$0")" && pwd)"
APP_HOME="$DIR"

# Find Java
if [ -n "$JAVA_HOME" ]; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

# Gradle distribution
GRADLE_VERSION="8.2"
GRADLE_URL="https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"
GRADLE_ZIP="$DIR/gradle/wrapper/gradle-${GRADLE_VERSION}-bin.zip"
GRADLE_HOME="$DIR/.gradle/wrapper/dists/gradle-${GRADLE_VERSION}-bin"

if [ ! -d "$GRADLE_HOME" ]; then
    echo "Downloading Gradle ${GRADLE_VERSION}..."
    mkdir -p "$GRADLE_HOME"
    if [ ! -f "$GRADLE_ZIP" ]; then
        curl -fsSL -o "$GRADLE_ZIP" "$GRADLE_URL"
    fi
    unzip -q -o "$GRADLE_ZIP" -d "$GRADLE_HOME"
fi

GRADLE_BIN="$GRADLE_HOME/gradle-${GRADLE_VERSION}/bin/gradle"

exec "$JAVACMD" \
    -classpath "$GRADLE_BIN/lib/gradle-launcher.jar" \
    org.gradle.launcher.GradleMain \
    "$@"
