
# <a name="benchmark-suite">Benchmark-Suite</a>

Benchmarks of different IMDG Products.  Focused on __Off-Heap__ capabilities.


## <a name="ProductList">Product List</a>

Vendor | Product | Version
--- |--- |---
Oracle | Coherence | 12.2.1-3-0
Pivotal | Apache Geode | 1.8.0
Hazelcast | Hazelcast IMDG | 3.11.1
GridGain | Apache Ignite | 2.7.0

### <a href="https://docs.google.com/spreadsheets/d/1fe1SrNEbHsCBv3hgQkNDkVdI72kRvKmubvnW-c7xzZI">Latest Benchmark Results</a>


## <a name="Build">Building the Benchmark Applications</a>

### Build all

```bash
gradle -Dorg.gradle.daemon=false clean buildAll
```

### Build and package each individual application

#### Coherence
  
```bash
gradle -Dorg.gradle.daemon=false coherence:clean coherence:buildAll
```

#### Geode

```bash
gradle -Dorg.gradle.daemon=false geode:clean geode:buildAll
```

#### Hazelcast


```bash
gradle -Dorg.gradle.daemon=false hazelcast:clean hazelcast:buildAll
```

#### Ignite

```bash
gradle -Dorg.gradle.daemon=false ignite:clean ignite:buildAll
```

## <a name="Deploy">Deploy the Applications</a>

### Hazelcast

  * Extract the libraries

```bash
cd ./hazelcast/build/distributions
tar xvf benchmark.hazelcast-1.0-SNAPSHOT.tar
cd ./bin
```

Start the cluster
  
  * Locally...
    
```bash
./startHazelcastMemberLocal.sh &
./startHazelcastMemberLocal.sh &
```

  * On Server(s)...
    
```bash
./startHazelcastMember.sh &
./startHazelcastMember.sh &
```
### Coherence

  * Extract the libraries

```bash
cd ./coherence/build/distributions
tar xvf benchmark.coherence-1.0-SNAPSHOT.tar
cd ./bin
```
Start the cluster

  * Locally...
  
```bash
./startCoherenceMemberLocal.sh &
./startCoherenceMemberLocal.sh &
```

  * On Server(s)...

```bash
./startCoherenceMember1.sh &
./startCoherenceMember2.sh &
```

### Geode

  * Extract the libraries

```bash
cd ./geode/build/distributions
tar xvf benchmark.geode-1.0-SNAPSHOT.tar
cd ./bin
```

Start the cluster

  * Locally...
  
```bash
./startMember1Local.sh &
./startMember2Local.sh &
```

  * On Server(s)...
```bash
./startMember1.sh &
./startMember2.sh &
```

### Ignite

  * Extract the libraries

```bash
cd ./ignite/build/distributions
tar xvf benchmark.ignite-1.0-SNAPSHOT.tar
cd ./bin
```

Start the cluster

  * Locally...
  
```bash
./startIgniteMemberLocal.sh &
./startIgniteMemberLocal.sh &
```

  * On Server(s)...
```bash
./startIgniteMember.sh &
./startIgniteMember.sh &
```

## <a name="RunBenchmarks">Running the Benchmarks</a>

#### Hazelcast

  * Locally
``` bash
./ runHazelcastBenchmarkLocal.sh
```

  * On Server
``` bash
./ runHazelcastBenchmark.sh
```

#### Coherence

  * Locally
  
``` bash
./ runCoherenceBenchmarkLocal.sh
```

  * On Server
  
``` bash
./ runCoherenceBenchmark.sh
```
#### Geode

  * Locally
  
``` bash
./ runGeodeBenchmarkLocal.sh
```

  * On Server
  
``` bash
./ runGeodeBenchmark.sh
```

#### Ignite

  * Locally
  
``` bash
./ runIgniteBenchmarkLocal.sh
```

  * On Server
  
``` bash
./ runIgniteBenchmark.sh
```

<a href="#benchmark-suite">Back to Top</a>


