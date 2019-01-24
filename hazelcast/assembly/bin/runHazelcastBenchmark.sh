#!/usr/bin/env bash

PRG="$0"
PRGDIR=`dirname "$PRG"`
HAZELCAST_HOME=`cd "$PRGDIR/.." >/dev/null; pwd`
WORK_DIRECTORY="$HAZELCAST_HOME/results"

APP_PID=$RANDOM
TODAY=`date +%Y-%m-%d.%H-%M-%S`

if [ ! -d "$WORK_DIRECTORY/logs" ]; then
  mkdir -p "$WORK_DIRECTORY/logs"
fi

CLASS_PATH="\
$HAZELCAST_HOME/conf:\
$HAZELCAST_HOME/benchmark.hazelcast-1.0-SNAPSHOT.jar:\
$HAZELCAST_HOME/lib/*"

MEM_OPTS="-Xms4g -Xmx4g -XX:+HeapDumpOnOutOfMemoryError"
GC_OPTS="\
-XX:+UseG1GC \
-XX:+PrintGCDetails \
-XX:+PrintGCDateStamps \
-XX:+PrintGCTimeStamps \
-Xloggc:$WORK_DIRECTORY/logs/hazelcast-gc.$TODAY.$APP_PID.log"

#-XX:+PrintCompilation -verbose:gc \

if [ -z ${HZ_USE_ASYNC_MAP_STREAMER+x} ];
    then export HZ_USE_ASYNC_MAP_STREAMER=false;
fi

JAVA_OPTS="-server -showversion \
-Dhazelcast.client.config=$HAZELCAST_HOME/conf/hazelcast-client.xml \
-Dhazelcast.system.log.enabled=true \
-Dbenchmark.useAsyncMapStreamer=$HZ_USE_ASYNC_MAP_STREAMER \
-Dbenchmark.record.count=1000000 \
-Dbenchmark.batch.size=5000 \
-Dbenchmark.range.percent=0.05 \
$MEM_OPTS \
$GC_OPTS"

JMH_OPTS="-wi 1 -t 1 -i 1 -f 1 -gc true  -rf json -rff $WORK_DIRECTORY/hazelcast.$TODAY.$APP_PID.json -o $WORK_DIRECTORY/hazelcast.$TODAY.$APP_PID.txt -jvmArgsAppend -ea"

COMMAND_LINE="java $JAVA_OPTS -cp $CLASS_PATH org.openjdk.jmh.Main JavaSerializableBenchmark $JMH_OPTS"
echo $COMMAND_LINE
$COMMAND_LINE
