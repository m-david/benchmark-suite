# Benchmark-Suite
Benchmarks of different IMDG Products.  Focused on Off-Heap capabilities.

##Product List

Vendor | Product | Version
--- |--- |---
Oracle | Coherence | 12.2.1-3-0
Pivotal | Apache Geode | 1.7.0
Hazelcast | Hazelcast IMDG | 3.10.5
GridGain | Apache Ignite | 2.7.0

## Building the Benchmark Applications

To build and package the application, run:

###Coherence

####Manual build
```bash
gradle -Dorg.gradle.daemon=false coherence:clean coherence:buildAll
```

###Geode

####Manual build
```bash
gradle -Dorg.gradle.daemon=false geode:clean geode:buildAll
```

###Hazelcast

####Manual build
```bash
gradle -Dorg.gradle.daemon=false hazelcast:clean hazelcast:buildAll
```

###Ignite

####Manual build
```bash
gradle -Dorg.gradle.daemon=false ignite:clean ignite:buildAll
```

##Running the Benchmarks

###Hazelcast

####Extract the libraries

```bash
cd ./hazelcast/build/distributions
tar xvf benchmark.hazelcast-1.0-SNAPSHOT.tar
cd ./bin
```
#### Start the cluster
##### Running Locally...
```bash
./startHazelcastMemberLocal.sh &
./startHazelcastMemberLocal.sh &
```

``` bash
./ runHazelcastBenchmarkLocal.sh
```

##### Running On Server(s)...
```bash
./startHazelcastMember.sh &
./startHazelcastMember.sh &
```

``` bash
./ runHazelcastBenchmark.sh
```
#### Run the Hazelcast benchmarks...
##### Running Locally
``` bash
./ runHazelcastBenchmarkLocal.sh
```

##### Running On Server
``` bash
./ runHazelcastBenchmark.sh
```
