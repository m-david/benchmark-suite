#!/usr/bin/env bash

PRG="$0"
PRGDIR=`dirname "$PRG"`
APP_HOME=`cd "$PRGDIR/.." >/dev/null; pwd`
WORK_DIRECTORY="$APP_HOME/results/logs"

APP_PID=$RANDOM
TODAY=`date +%Y-%m-%d.%H-%M-%S`

if [ ! -d "$WORK_DIRECTORY" ]; then
  mkdir -p "$WORK_DIRECTORY"
fi

CLASS_PATH=\
$APP_HOME/conf:\
$APP_HOME/jmh-lib/benchmark.coherence-1.0-SNAPSHOT-jmh.jar:\
$APP_HOME/benchmark.coherence-1.0-SNAPSHOT.jar:\
$APP_HOME/lib/*

MEM_OPTS="-Xms2g -Xmx2g -XX:+HeapDumpOnOutOfMemoryError"
GC_OPTS="\
-XX:+UseG1GC \
-XX:+PrintGCDetails \
-XX:+PrintGCDateStamps \
-XX:+PrintGCTimeStamps \
-Xloggc:$WORK_DIRECTORY/coherence-gc.$TODAY.$APP_PID.log"

#-XX:+PrintCompilation -verbose:gc \

JAVA_OPTS="\
-server -Djava.net.preferIPv4Stack=true -showversion \
-Dtangosol.coherence.cacheconfig=tangosol-java-client-config.xml \
-Dtangosol.coherence.proxy.address=10.212.1.117 \
-Dtangosol.pof.config=my-custom-pof-config.xml \
-Dbenchmark.record.count=1000000 \
-Dbenchmark.batch.size=5000 \
-Dbenchmark.range.percent=0.05 \
$MEM_OPTS $GC_OPTS"


JMH_OPTS="-wi 1 -t 1 -i 1 -f 1 -gc true  -rf json -rff $APP_HOME/results/coherence.$TODAY.$APP_PID.json -o $APP_HOME/results/coherence.$TODAY.$APP_PID.txt -jvmArgsAppend -ea"

JAVA_OPTS="$JAVA_OPTS $1 $2"

java $JAVA_OPTS -cp $CLASS_PATH org.openjdk.jmh.Main CoherenceUseCasesBenchmark $JMH_OPTS
