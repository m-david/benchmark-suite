#!/usr/bin/env bash

PRG="$0"
PRGDIR=`dirname "$PRG"`
HAZELCAST_HOME=`cd "$PRGDIR/.." >/dev/null; pwd`
WORK_DIRECTORY="$HAZELCAST_HOME/logs"

if [ ! -d "$WORK_DIRECTORY" ]; then
  mkdir -p "$WORK_DIRECTORY"
fi

MEM_OPTS="-Xms512m -Xmx512m -XX:+HeapDumpOnOutOfMemoryError"
GC_OPTS="-XX:+UseG1GC -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -Xloggc:$WORK_DIRECTORY/hazelcast-gc.log"
JAVA_OPTS="-server -showversion \
-Dhazelcast.config=$HAZELCAST_HOME/conf/hazelcast-server-local.xml \
$MEM_OPTS $GC_OPTS"

CLASS_PATH="\
$HAZELCAST_HOME/conf:\
$HAZELCAST_HOME/benchmark.hazelcast-1.0-SNAPSHOT.jar:\
$HAZELCAST_HOME/lib/*"

COMMAND_LINE="java $JAVA_OPTS -cp $CLASS_PATH  com.hazelcast.poc.Member"
echo $COMMAND_LINE
$COMMAND_LINE
