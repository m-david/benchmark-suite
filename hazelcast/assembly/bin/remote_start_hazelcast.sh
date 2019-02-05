#!/usr/bin/env bash

if [ "x$SERVER_LIST" = "x" ]; then
    echo "Please set env variable SERVER_LIST first! Space separated."
    exit
else
    export SERVERS=$SERVER_LIST
fi


for sname in $SERVERS
do
    echo "Starting Hazelcast on $name ..."
	echo ssh $sname "/home/ec2-user/benchmark/hazelcast/build/distribution/bin/startAwsHazelcast.sh &; sleep 20; exit "
	echo "Started Hazelcast."
done
