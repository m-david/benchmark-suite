
# <a name="benchmark-suite">Benchmark-Suite</a>

Benchmarks of different IMDG Products.  Focused on __Off-Heap__ capabilities.

<a href="https://github.com/hazelcast/coherence-hazelcast-benchmark-suite">Please refer to original project:  Coherence-hazelcast-benchmark-suite</a>


## <a name="ProductList">Product List</a>

Vendor | Product | Version
--- |--- |---
Oracle | Coherence | 12.2.1-3-0
Pivotal | Apache Geode | 1.8.0
Hazelcast | Hazelcast IMDG | 3.11.1
GridGain | Apache Ignite | 2.7.0

### <a href="https://docs.google.com/spreadsheets/d/1fe1SrNEbHsCBv3hgQkNDkVdI72kRvKmubvnW-c7xzZI">Latest Benchmark Results</a>
### <a href="https://drive.google.com/drive/folders/1oMd3BLAEHgr6edA7OzUxMekaeOZHXpTY">Raw JMH Benchmark Results</a>

## <a name="test-strategy">Test Strategy</a>

Since Ignite does not allow __On-Heap Only__, all the cached maps are configured for __Off-Heap__ only.  Benchmark is configured for Sampling for 100K iterations measured in __microseconds__.

The following operations are benchmarked:

Description | Method Name
--- |---
Insert single Trade |  #b01_insertTradeSingle
Insert bulk Trades |  #b02_insertTradeBulk
Get single Trade |  #b03_getTradeSingle
One attribute trade query | #b04_getTradeOneFilter
Three attribute trade query | #b05_getTradesThreeFilter
One attribute (indexed) trade query | #b06_getTradeIndexedFilter
Get trades by id using range query | #b07_getTradeIdRangeFilter 

## <a name="Build">Building the Benchmark Applications</a>

### Build all

```bash
gradle -Dorg.gradle.daemon=false clean buildAll
```

### Build and package each individual application

#### Coherence

##### Due to Oracle licensing constraints, coherence is not available in any maven repository.  Therefore, you must download it first from Oracle Downloads.
  1. <a href="https://www.oracle.com/technetwork/middleware/coherence/downloads/coherence-archive-165749.html">Download the latest version</a>
  2. Install the archive locally to your computer and install into maven repository.  Please note that you must update specified version in ./coherence/build.properties if it has changed.  And, example below needs to be updated as well to reflect this.
  3. Example:
  
  ```bash
  $java -jar fmw_12.2.1.3.0_coherence.jar
  $cd ~/Oracle_home/Middleware/Oracle_home
  $mvn install:install-file -Dfile=./coherence/lib/coherence.jar -DgroupId=com.oracle.coherence -DartifactId=coherence -Dversion=12.2.1.3.0 -Dpackaging=jar
  ```
Now, you can build it ...
   
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

  * Pre-Install Requirements
    * Enterprise HD License required.  If you do not have one, you will need to ask Sales for trial license.
    * edit your .bash_profile to add the following ...

```bash
export HAZELCAST_LICENSE_KEY=XXXXXXXXXXXXXXXXXXXXXXXXX
```

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


