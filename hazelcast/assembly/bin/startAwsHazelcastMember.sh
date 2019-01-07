#!/usr/bin/env bash

PRG="$0"
PRGDIR=`dirname "$PRG"`
HAZELCAST_HOME=`cd "$PRGDIR/.." >/dev/null; pwd`
WORK_DIRECTORY="$HAZELCAST_HOME/logs"

APP_PID=$RANDOM
TODAY=`date +%Y-%m-%d.%H-%M-%S`

if [ ! -d "$WORK_DIRECTORY" ]; then
  mkdir -p "$WORK_DIRECTORY"
fi

MEM_OPTS="-Xms4g -Xmx4g -XX:+HeapDumpOnOutOfMemoryError"
GC_OPTS="-XX:+UseG1GC -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -Xloggc:$WORK_DIRECTORY/hazelcast-gc.$TODAY.$APP_PID.log"
JAVA_OPTS="-server -showversion \
-Dhazelcast.config=$HAZELCAST_HOME/conf/hazelcast-server-aws.xml \
-Dhazelcast.index.copy.behavior=NEVER \
-Dhazelcast.enterprise.license.key=$HAZELCAST_LICENSE_KEY \
$MEM_OPTS $GC_OPTS"

CLASS_PATH="\
$HAZELCAST_HOME/conf:\
$HAZELCAST_HOME/benchmark.hazelcast-1.0-SNAPSHOT.jar:\
$HAZELCAST_HOME/lib/*"

COMMAND_LINE="java $JAVA_OPTS -cp $CLASS_PATH  com.hazelcast.poc.Member"
echo $COMMAND_LINE
$COMMAND_LINE
