# Benchmark-Suite
Benchmarks of different IMDG Products.  Focused on Off-Heap capabilities.

## Product List

Vendor | Product | Version
--- |--- |---
Oracle | Coherence | 12.2.1-3-0
Pivotal | Apache Geode | 1.7.0
Hazelcast | Hazelcast IMDG | 3.11.1
GridGain | Apache Ignite | 2.7.0

## Building the Benchmark Applications

To build and package the application

### Coherence

#### Manual build
```bash
gradle -Dorg.gradle.daemon=false coherence:clean coherence:buildAll
```

### Geode

#### Manual build
```bash
gradle -Dorg.gradle.daemon=false geode:clean geode:buildAll
```

### Hazelcast

#### Manual build
```bash
gradle -Dorg.gradle.daemon=false hazelcast:clean hazelcast:buildAll
```

### Ignite

#### Manual build
```bash
gradle -Dorg.gradle.daemon=false ignite:clean ignite:buildAll
```

## Running the Benchmarks

### Hazelcast

#### Extract the libraries

```bash
cd ./hazelcast/build/distributions
tar xvf benchmark.hazelcast-1.0-SNAPSHOT.tar
cd ./bin
```
#### Start the cluster
##### Locally...
```bash
./startHazelcastMemberLocal.sh &
./startHazelcastMemberLocal.sh &
```
##### On Server(s)...
```bash
./startHazelcastMember.sh &
./startHazelcastMember.sh &
```
#### Run the Hazelcast benchmarks...

##### Locally
``` bash
./ runHazelcastBenchmarkLocal.sh
```

##### On Server
``` bash
./ runHazelcastBenchmark.sh
```
### Coherence

#### Extract the libraries

```bash
cd ./coherence/build/distributions
tar xvf benchmark.coherence-1.0-SNAPSHOT.tar
cd ./bin
```
#### Start the cluster
##### Locally...
```bash
./startCoherenceMemberLocal.sh &
./startCoherenceMemberLocal.sh &
```

##### On Server(s)...
```bash
./startCoherenceMember1.sh &
./startCoherenceMember2.sh &
```

#### Run the Coherence benchmarks...
##### Locally
``` bash
./ runCoherenceBenchmarkLocal.sh
```

##### On Server
``` bash
./ runCoherenceBenchmark.sh
```

### Hazelcast

#### Extract the libraries

```bash
cd ./hazelcast/build/distributions
tar xvf benchmark.hazelcast-1.0-SNAPSHOT.tar
cd ./bin
```
#### Start the cluster
##### Locally...
```bash
./startHazelcastMemberLocal.sh &
./startHazelcastMemberLocal.sh &
```
##### On Server(s)...
```bash
./startHazelcastMember.sh &
./startHazelcastMember.sh &
```
#### Run the Hazelcast benchmarks...

##### Locally
``` bash
./ runHazelcastBenchmarkLocal.sh
```

##### On Server
``` bash
./ runHazelcastBenchmark.sh
```






