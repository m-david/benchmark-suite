#!/bin/bash

PRG="$0"
PRGDIR=`dirname "$PRG"`
APP_HOME=`cd "$PRGDIR/.." >/dev/null; pwd`

WORK_DIRECTORY="$APP_HOME/server1/logs"
if [ ! -d "$WORK_DIRECTORY" ]; then
  mkdir -p "$WORK_DIRECTORY"
fi

CLASS_PATH=\
$APP_HOME/conf:\
$APP_HOME/benchmark.coherence-1.0-SNAPSHOT.jar:\
$APP_HOME/lib/*

MEM_OPTS="-Xms512m -Xmx512m -XX:+HeapDumpOnOutOfMemoryError"
GC_OPTS="-XX:+UseG1GC -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -Xloggc:$WORK_DIRECTORY/coherence-gc.log"
JAVA_OPTS="\
-server -Djava.net.preferIPv4Stack=true -showversion \
-Dtangosol.coherence.cacheconfig=cache-configuration.xml \
-Dtangosol.pof.enabled=true \
-Dtangosol.pof.config=my-custom-pof-config.xml \
-Dtangosol.coherence.management=local-only
-Dtangosol.coherence.override=tangosol-coherence-override.xml \
-Dtangosol.coherence.extend.host=10.212.1.117 \
-Dbenchmark.off-heap.scheme.name=BENCHMARK-Partitioned-Off-Heap-Large \
-Dtangosol.coherence.wka=10.212.1.117 \
-Dtangosol.coherence.wka.port=9090 \
-Dtangosol.coherence.localhost=10.212.1.117 \
-Dbenchmark.off-heap-large.auto-start=true \
$MEM_OPTS \
$GC_OPTS"

#-Dtangosol.coherence.localhost=localhost \
#-Dtangosol.coherence.localport=9090 \
#-Dtangosol.coherence.localport.adjust=true \

JAVA_OPTS="$JAVA_OPTS $1 $2"

COMMAND_LINE="java $JAVA_OPTS -cp $CLASS_PATH com.tangosol.net.DefaultCacheServer"
echo $COMMAND_LINE
$COMMAND_LINE
