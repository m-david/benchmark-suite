#!/usr/bin/env bash

if [ $1x = "cleanx" ]; then
  CLEAN_ARG="clean"
else
  CLEAN_ARG=""
fi

COMMAND_LINE="gradle -Dorg.gradle.daemon=false \
$CLEAN_ARG \
hazelcast:buildAll \
geode:buildAll \
coherence:buildAll \
ignite:buildAll"

if [ ! -d "./build" ]; then
  mkdir -p "./build"
  echo "created ./build directory"
else
  echo "./build directory already exists"
fi

echo $COMMAND_LINE
$COMMAND_LINE

COMMAND_LINE="tar czvf ./build/benchmark-suite.tar.gz \
 ./geode/build/distributions/benchmark.geode-1.0-SNAPSHOT.tar \
 ./hazelcast/build/distributions/benchmark.hazelcast-1.0-SNAPSHOT.tar \
 ./coherence/build/distributions/benchmark.coherence-1.0-SNAPSHOT.tar \
 ./ignite/build/distributions/benchmark.ignite-1.0-SNAPSHOT.tar"

echo $COMMAND_LINE
$COMMAND_LINE

