#!/usr/bin/env bash

PRG="$0"
PRGDIR=`dirname "$PRG"`
APP_HOME=`cd "$PRGDIR/.." >/dev/null; pwd`
WORK_DIRECTORY="$APP_HOME/results/logs"

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
-XX:+PrintCompilation -verbose:gc \
-Xloggc:$WORK_DIRECTORY/coherence-gc.log"

JAVA_OPTS="-server -showversion $MEM_OPTS $GC_OPTS \
-Dtangosol.coherence.cacheconfig=tangosol-java-client-config.xml \
-Dtangosol.pof.config=my-custom-pof-config.xml"

JMH_OPTS="-wi 1 -i 1 -f 2 -gc true  -rf json -rff $APP_HOME/results/coherence.json -o $APP_HOME/results/coherence.txt -jvmArgsAppend -ea"

JAVA_OPTS="$JAVA_OPTS $1 $2"

java $JAVA_OPTS -cp $CLASS_PATH org.openjdk.jmh.Main CoherenceUseCasesBenchmark $JMH_OPTS
