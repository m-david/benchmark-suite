#!/usr/bin/env bash

gradle clean \
 hazelcast:buildAll \
 geode:buildAll \
 coherence:buildAll \
 ignite:buildAll

if [ ! -d "./build" ]; then
  mkdir -p "./build"
fi

tar czvf ./build/benchmark-suite.tar.gz \
 ./geode/build/distributions/benchmark.geode-1.0-SNAPSHOT.tar \
 ./hazelcast/build/distributions/benchmark.hazelcast-1.0-SNAPSHOT.tar \
 ./coherence/build/distributions/benchmark.coherence-1.0-SNAPSHOT.tar \
 ./ignite/build/distributions/benchmark.ignite-1.0-SNAPSHOT.tar

#/Users/mdavid/Downloads/apache-geode-1.7.0-src.tgz /Users/mdavid/Downloads/apache-ignite-2.7.0-src.zip
