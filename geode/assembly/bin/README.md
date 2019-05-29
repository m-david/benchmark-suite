# Build
Please refer to <a href="https://github.com/m-david/benchmark-suite">Benchmark-Suite Home Page</a>

* Deploy ./geode/build/distributions/benchmark.geode-1.0-SNAPSHOT.tar 
by untarring to a directory.


## Cheat Sheet for Running Scripts


## Start Geode Locator:

```$xslt
Usage : ./startLocator.sh \
    -b <bind-address> \
    -n <locator-name> \
    -s <server-port> \
    -l <locators-list> \
    -t <http-port-for-locator-only> \
    -j <jmx-port> \
    -h <off-heap-size> \
    
```

### Start Locally:
```
$ ./startLocator.sh -b 127.0.0.1 -n locator1 -s 10680 -l 127.0.0.1[10680] -t 8099 -j 1099 -o 256m -h 256m &
```

## Start Geode Member

```$xslt
	Usage : ./startMember.sh 
		-b <bind-address: 127.0.0.1>
		-f <writeSync-fsync-true-or-false> 
		-n <server-name: my-server-1> 
		-x <cache-xml: gemfire-server.xml> 
		-l <locators: 127.0.0.1[10680]> 
		-s <server-port: 10680> 
		-j <jmx-port: 2099> 
		-t <http-port: 8099 (locator only)> 
		-o <off-heap-size: 512M> 
		-h <heap-size: 512M> 
		-f <disk-sync: true or false> 
		-u <usage: this> 

```

### Start Locally:

### Node 1:
```
$ ./startMember.sh -b 127.0.0.1 -n server1 -s 40405 -l 127.0.0.1[10680] -j 2099 -o 512m -h 512m -x geode-server-payload.xml
```

### Node 2:

```
$ ./startMember.sh -b 127.0.0.1 -n server2 -s 40406 -l 127.0.0.1[10680] -j 2098 -o 512m -h 512m -x geode-server-payload.xml
```

## Payload Benchmark Test

```$xslt
Usage:  ./runPayload.sh -m <mode> -s <size-in-bytes> -r <record-count>
```

Modes:
  * Normal
  * Overflow
  * OffHeap
  * Persistent
  * PersistentOverflow

## The below runs are various modes, record size: 5K, record count: 200,000

### Normal

```$xslt
./startLocator.sh -b 127.0.0.1 -n locator1 -s 10680 -l 127.0.0.1[10680] -t 8099 -j 1099 -o 256m -h 256m &
./startMember.sh -b 127.0.0.1 -n server1 -s 40405 -l 127.0.0.1[10680] -j 2099 -o 128m -h 1500m -x geode-server-payload.xml &
./startMember.sh -b 127.0.0.1 -n server2 -s 40406 -l 127.0.0.1[10680] -j 2098 -o 128m -h 1500m -x geode-server-payload.xml &
./runPayload.sh -m Normal -s 5120 -r 200000 &
```

### OffHeap
```$xslt
./startLocator.sh -b 127.0.0.1 -n locator1 -s 10680 -l 127.0.0.1[10680] -t 8099 -j 1099 -o 256m -h 256m &
./startMember.sh -b 127.0.0.1 -n server1 -s 40405 -l 127.0.0.1[10680] -j 2099 -o 2g -h 512m -x geode-server-payload.xml &
./startMember.sh -b 127.0.0.1 -n server2 -s 40406 -l 127.0.0.1[10680] -j 2098 -o 2g -h 512m -x geode-server-payload.xml &
./runPayload.sh -m OffHeap -s 5120 -r 200000 &
```

### Overflow
```$xslt
./startLocator.sh -b 127.0.0.1 -n locator1 -s 10680 -l 127.0.0.1[10680] -t 8099 -j 1099 -o 256m -h 256m &
./startMember.sh -b 127.0.0.1 -n server1 -s 40405 -l 127.0.0.1[10680] -j 2099 -o 2g -h 512m -x geode-server-payload.xml &
./startMember.sh -b 127.0.0.1 -n server2 -s 40406 -l 127.0.0.1[10680] -j 2098 -o 2g -h 512m -x geode-server-payload.xml &
./runPayload.sh -m Overflow -s 5120 -r 200000 &
```

### Persistent-Async
```$xslt
./startLocator.sh -b 127.0.0.1 -n locator1 -s 10680 -l 127.0.0.1[10680] -t 8099 -j 1099 -o 256m -h 256m &
./startMember.sh -b 127.0.0.1 -n server1 -s 40405 -l 127.0.0.1[10680] -j 2099 -o 2g -h 512m -x geode-server-payload.xml &
./startMember.sh -b 127.0.0.1 -n server2 -s 40406 -l 127.0.0.1[10680] -j 2098 -o 2g -h 512m -x geode-server-payload.xml &
./runPayload.sh -m Persistent -s 5120 -r 200000 &
```

### Persistent-Sync
```$xslt
./startLocator.sh -b 127.0.0.1 -n locator1 -s 10680 -l 127.0.0.1[10680] -t 8099 -j 1099 -o 256m -h 256m &
./startMember.sh -b 127.0.0.1 -f true -n server1 -s 40405 -l 127.0.0.1[10680] -j 2099 -o 2g -h 512m -x geode-server-payload.xml &
./startMember.sh -b 127.0.0.1 -f true -n server2 -s 40406 -l 127.0.0.1[10680] -j 2098 -o 2g -h 512m -x geode-server-payload.xml &
./runPayload.sh -m Persistent -s 5120 -r 200000 &
```

Notes:
  * On AWS, due to frequent timeouts, increased heap-size to 2g
  
  
  
### PersistentOverflow-Async
```$xslt
./startLocator.sh -b 127.0.0.1 -n locator1 -s 10680 -l 127.0.0.1[10680] -t 8099 -j 1099 -o 256m -h 256m &
./startMember.sh -b 127.0.0.1 -n server1 -s 40405 -l 127.0.0.1[10680] -j 2099 -o 2g -h 512m -x geode-server-payload.xml &
./startMember.sh -b 127.0.0.1 -n server2 -s 40406 -l 127.0.0.1[10680] -j 2098 -o 2g -h 512m -x geode-server-payload.xml &
./runPayload.sh -m PersistentOverflow -s 5120 -r 200000 &
```

### PersistentOverflow-Sync
```$xslt
./startLocator.sh -b 127.0.0.1 -n locator1 -s 10680 -l 127.0.0.1[10680] -t 8099 -j 1099 -o 256m -h 256m &
./startMember.sh -b 127.0.0.1 -f true -n server1 -s 40405 -l 127.0.0.1[10680] -j 2099 -o 2g -h 512m -x geode-server-payload.xml &
./startMember.sh -b 127.0.0.1 -f true -n server2 -s 40406 -l 127.0.0.1[10680] -j 2098 -o 2g -h 512m -x geode-server-payload.xml &
./runPayload.sh -m PersistentOverflow -s 5120 -r 200000 &
```

