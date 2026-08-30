#!/bin/sh

APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
JAVA_CMD="${JAVA_HOME:+$JAVA_HOME/bin/}java"

if [ ! -f "$WRAPPER_JAR" ]; then
    echo "Gradle Wrapper JAR is missing: $WRAPPER_JAR" >&2
    exit 1
fi

exec "$JAVA_CMD" -classpath "$WRAPPER_JAR" org.gradle.wrapper.GradleWrapperMain "$@"
