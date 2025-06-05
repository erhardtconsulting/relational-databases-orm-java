#!/usr/bin/env bash

echo "+++ Starting application..."

set -eux

exec /opt/java/openjdk/bin/java $JAVA_OPTS -jar /app/app.jar