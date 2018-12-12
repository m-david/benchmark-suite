#!/usr/bin/env bash

PRG="$0"
PRGDIR=`dirname "$PRG"`
APP_HOME=`cd "$PRGDIR/.." >/dev/null; pwd`

WORK_DIRECTORY="$APP_HOME/server1/logs"
if [ ! -d "$WORK_DIRECTORY" ]; then
  mkdir -p "$WORK_DIRECTORY"
fi

MEM_OPTS="-Xms512m -Xmx512m -XX:+HeapDumpOnOutOfMemoryError"
GC_OPTS="-XX:+UseG1GC -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -Xloggc:$WORK_DIRECTORY/ignite-gc.log"
JAVA_OPTS="-server -showversion \
-Dbenchmark.ignite.config.xml=$APP_HOME/conf/ignite-cache.xml \
-Dbenchmark.ignite.tcp.discovery.address=10.212.1.117:47500..47509 \
$MEM_OPTS $GC_OPTS"

CLASS_PATH="\
$APP_HOME/conf:\
$APP_HOME/benchmark.ignite-1.0-SNAPSHOT.jar:\
$APP_HOME/lib/*"

COMMAND_LINE="java $JAVA_OPTS -cp $CLASS_PATH  com.ignite.poc.Member"
echo $COMMAND_LINE
$COMMAND_LINE