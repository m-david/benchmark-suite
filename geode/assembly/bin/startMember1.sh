#!/usr/bin/env bash

PRG="$0"
PRGDIR=`dirname "$PRG"`
APP_HOME=`cd "$PRGDIR/.." >/dev/null; pwd`

SERVER_HOME=$APP_HOME/server1
if [ ! -d "$SERVER_HOME" ]; then
  mkdir -p "$SERVER_HOME"
fi

cd $SERVER_HOME

MEMBER_NAME=server1
WORK_DIRECTORY="$SERVER_HOME/logs"

APP_PID=$RANDOM
TODAY=`date +%Y-%m-%d.%H-%M-%S`

if [ ! -d "$WORK_DIRECTORY" ]; then
  mkdir -p "$WORK_DIRECTORY"
fi

CLASS_PATH="$APP_HOME/benchmark.geode-1.0-SNAPSHOT.jar:$APP_HOME/conf:$APP_HOME/lib/*"

APP_PID=$RANDOM
TODAY=`date +%Y-%m-%d.%H-%M-%S`

MEM_OPTS="-Xms512m -Xmx512m -XX:+HeapDumpOnOutOfMemoryError"
GC_OPTS="-XX:+UseG1GC -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -Xloggc:$WORK_DIRECTORY/geode-gc.$TODAY.$APP_PID.log"
JAVA_OPTS="-server -showversion \
-Dgemfire.log-file=$WORK_DIRECTORY/geode.$TODAY.$APP_PID.log \
-Dgemfire.locators=10.212.1.116[10680] \
-Dgemfire.cache-xml-file=$APP_HOME/conf/geode-server.xml \
-Dgemfire.server-port=40405 \
-Dgemfire.jmx-manager-port=2099 \
-Dgemfire.bind-address=10.212.1.117 \
-Dgemfire.name=$MEMBER_NAME \
-Dgemfire.off-heap-memory-size=4G \
-Dgemfire.critical-off-heap-percentage=90 \
-Dgemfire.eviction-off-heap-percentage=80 \
$MEM_OPTS $GC_OPTS"

COMMAND_LINE="java $JAVA_OPTS -cp $CLASS_PATH  com.geode.poc.GeodeMember"
echo $COMMAND_LINE
$COMMAND_LINE
