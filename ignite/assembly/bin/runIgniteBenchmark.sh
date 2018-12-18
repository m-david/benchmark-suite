#!/usr/bin/env bash

PRG="$0"
PRGDIR=`dirname "$PRG"`
APP_HOME=`cd "$PRGDIR/.." >/dev/null; pwd`
WORK_DIRECTORY="$APP_HOME/results"

APP_PID=$RANDOM
TODAY=`date +%Y-%m-%d.%H-%M-%S`

if [ ! -d "$WORK_DIRECTORY/logs" ]; then
  mkdir -p "$WORK_DIRECTORY/logs"
fi

CLASS_PATH="\
$APP_HOME/conf:\
$APP_HOME/jmh-lib/benchmark.ignite-1.0-SNAPSHOT-jmh.jar:\
$APP_HOME/lib/*"

MEM_OPTS="-Xms2g -Xmx2g -XX:+HeapDumpOnOutOfMemoryError"
GC_OPTS="\
-XX:+UseG1GC \
-XX:+PrintGCDetails \
-XX:+PrintGCDateStamps \
-XX:+PrintGCTimeStamps \
-Xloggc:$WORK_DIRECTORY/logs/ignite-gc.$TODAY.$APP_PID.log"

# -XX:+PrintCompilation -verbose:gc \

if [ -z ${HZ_USE_ASYNC_MAP_STREAMER+x} ];
    then export HZ_USE_ASYNC_MAP_STREAMER=false;
fi

JAVA_OPTS="-server -showversion \
-Dbenchmark.ignite.discovery.addresses=10.212.1.117 \
-Dbenchmark.ignite.discovery.ports=48500..48501 \
-DIGNITE_QUIET=false \
$GC_OPTS"

JMH_OPTS="-wi 1 -i 1 -f 2 -gc true  -rf json -rff $WORK_DIRECTORY/ignite.$TODAY.$APP_PID.json -o $WORK_DIRECTORY/ignite.$TODAY.$APP_PID.txt -jvmArgsAppend -ea"

COMMAND_LINE="java $JAVA_OPTS -cp $CLASS_PATH org.openjdk.jmh.Main IgniteUseCasesBenchmark $JMH_OPTS"
echo $COMMAND_LINE
$COMMAND_LINE
