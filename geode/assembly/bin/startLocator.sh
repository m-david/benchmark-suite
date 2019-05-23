#!/usr/bin/env bash

. ./cacheargs
#
#	Usage : ./geode/assembly/bin/cacheargs
#		-b <bind-address: 127.0.0.1>
#		-n <server-name: my-server-1>
#		-x <cache-xml: gemfire-server.xml>
#		-l <locators: 127.0.0.1[10680]>
#		-s <server-port: 10680>
#		-j <jmx-port: 2099>
#		-t <http-port: 8099 (locator only)>
#		-o <off-heap-size: 512M>
#		-h <heap-size: 512M>
#		-u <usage: this>

PRG="$0"
PRGDIR=`dirname "$PRG"`
##APP_HOME=`cd "$PRGDIR/.." >/dev/null; pwd`
APP_HOME=`cd "$PRGDIR/.." >/dev/null; pwd`

SERVER_HOME=$APP_HOME/$SRV_NAME
if [ ! -d "$SERVER_HOME" ]; then
  mkdir -p "$SERVER_HOME"
fi

cd $SERVER_HOME

MEMBER_NAME=$SRV_NAME
WORK_DIRECTORY="$SERVER_HOME/logs"

APP_PID=$RANDOM
TODAY=`date +%Y-%m-%d.%H-%M-%S`

if [ ! -d "$WORK_DIRECTORY" ]; then
  mkdir -p "$WORK_DIRECTORY"
fi

CLASS_PATH="$APP_HOME/benchmark.geode-1.0-SNAPSHOT.jar:$APP_HOME/conf:$APP_HOME/lib/*"
APP_PID=$RANDOM
TODAY=`date +%Y-%m-%d.%H-%M-%S`

MEM_OPTS="-Xms$HEAP_SIZE -Xmx$HEAP_SIZE -XX:+HeapDumpOnOutOfMemoryError"
GC_OPTS="-XX:+UseG1GC -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -Xloggc:$WORK_DIRECTORY/geode-gc.$TODAY.$APP_PID.log"
JAVA_OPTS="-server -showversion \
-Dgemfire.log-file=$WORK_DIRECTORY/geode.$TODAY.$APP_PID.log \
-Dgemfire.locators=$LOCATORS \
-Dgemfire.locator-port=$SRV_PORT \
-Dgemfire.jmx-manager-port=$JMX_PORT \
-Dgemfire.jmx-manager-http-port=$HTTP_PORT \
-Dgemfire.bind-address=$BIND_ADDR \
-Dgemfire.name=$MEMBER_NAME \
$MEM_OPTS $GC_OPTS"

COMMAND_LINE="java $JAVA_OPTS -cp $CLASS_PATH  com.geode.poc.GeodeLocator"
echo $COMMAND_LINE
$COMMAND_LINE
