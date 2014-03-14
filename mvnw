#!/bin/sh
# ----------------------------------------------------------------------------
# Maven Wrapper - simplified, auto-generated stub.
# Provides ./mvnw so contributors don't need Maven installed globally.
# ----------------------------------------------------------------------------
BASE_DIR=$(cd "$(dirname "$0")" && pwd)
WRAPPER_JAR="$BASE_DIR/.mvn/wrapper/maven-wrapper.jar"
WRAPPER_PROPERTIES="$BASE_DIR/.mvn/wrapper/maven-wrapper.properties"

if [ ! -f "$WRAPPER_JAR" ]; then
    echo "Downloading Maven Wrapper..."
    javac "$BASE_DIR/.mvn/wrapper/MavenWrapperDownloader.java"
    (cd "$BASE_DIR/.mvn/wrapper" && java MavenWrapperDownloader "$BASE_DIR")
fi

exec java -jar "$WRAPPER_JAR" "$@"
