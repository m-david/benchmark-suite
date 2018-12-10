#!/usr/bin/env bash

PRG="$0"
PRGDIR=`dirname "$PRG"`
APP_HOME=`cd "$PRGDIR/.." >/dev/null; pwd`
#$JAVA_OPTS=
CLASS_PATH="$APP_HOME/benchmark.geode-1.0-SNAPSHOT.jar:$APP_HOME/conf:$APP_HOME/lib/*:$GEMFIRE_HOME/lib/*:"

gfsh start server \
    --dir=$APP_HOME/server1 --locators=localhost[10680] \
    --classpath=$CLASS_PATH \
    --properties-file=$APP_HOME/conf/geode-server.properties \
    --cache-xml-file=$APP_HOME/conf/geode-server.xml \
    --server-port=40405 \
    --name=server1 \
    --initial-heap=512M \
    --max-heap=512M \
    --off-heap-memory-size=512M \
    --critical-off-heap-percentage=90 --eviction-off-heap-percentage=80
