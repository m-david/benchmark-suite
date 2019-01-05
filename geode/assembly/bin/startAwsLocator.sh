#!/usr/bin/env bash

PRG="$0"
PRGDIR=`dirname "$PRG"`
APP_HOME=`cd "$PRGDIR/.." >/dev/null; pwd`

SERVER_HOME=$APP_HOME/locator1
if [ ! -d "$SERVER_HOME" ]; then
  mkdir -p "$SERVER_HOME"
fi

cd $SERVER_HOME

MEMBER_NAME=locator1
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
-Dgemfire.locator-port=10680 \
-Dgemfire.jmx-manager-port=1099 \
-Dgemfire.jmx-manager-http-port=8099 \
-Dgemfire.bind-address=172.30.0.90 \
-Dgemfire.name=$MEMBER_NAME \
$MEM_OPTS $GC_OPTS"

COMMAND_LINE="java $JAVA_OPTS -cp $CLASS_PATH  com.geode.poc.GeodeLocator"
echo $COMMAND_LINE
$COMMAND_LINE
