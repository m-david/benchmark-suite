#!/usr/bin/env bash

PRG="$0"
PRGDIR=`dirname "$PRG"`
HAZELCAST_HOME=`cd "$PRGDIR/.." >/dev/null; pwd`
WORK_DIRECTORY="$HAZELCAST_HOME/results"

if [ ! -d "$WORK_DIRECTORY/logs" ]; then
  mkdir -p "$WORK_DIRECTORY/logs"
fi

CLASS_PATH="\
$HAZELCAST_HOME/conf:\
$HAZELCAST_HOME/jmh-lib/benchmark.ignite-1.0-SNAPSHOT-jmh.jar:\
$HAZELCAST_HOME/lib/*"

MEM_OPTS="-Xms2g -Xmx2g -XX:+HeapDumpOnOutOfMemoryError"
GC_OPTS="\
-XX:+UseG1GC \
-XX:+PrintGCDetails \
-XX:+PrintGCDateStamps \
-XX:+PrintGCTimeStamps \
-XX:+PrintCompilation -verbose:gc \
-Xloggc:$WORK_DIRECTORY/logs/ignite-gc.log"

if [ -z ${HZ_USE_ASYNC_MAP_STREAMER+x} ];
    then export HZ_USE_ASYNC_MAP_STREAMER=false;
fi

JAVA_OPTS="-server -showversion \
-Dbenchmark.ignite.addresses=127.0.0.1:10800 \
-DIGNITE_QUIET=false \
$GC_OPTS"

JMH_OPTS="-wi 1 -i 1 -f 2 -gc true  -rf json -rff $WORK_DIRECTORY/ignite.json -o $WORK_DIRECTORY/ignite.txt -jvmArgsAppend -ea"

COMMAND_LINE="java $JAVA_OPTS -cp $CLASS_PATH org.openjdk.jmh.Main IgniteUseCasesBenchmark $JMH_OPTS"
echo $COMMAND_LINE
$COMMAND_LINE
