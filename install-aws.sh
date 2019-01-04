#!/usr/bin/env bash

scp -i ~/.ssh/id_rsa -o StrictHostKeyChecking=no ./benchmark-suite.tar.gz ec2-user@172.30.0.205:/home/ec2-user/benchmark
scp -i ~/.ssh/id_rsa -o StrictHostKeyChecking=no ./benchmark-suite.tar.gz ec2-user@172.30.0.44:/home/ec2-user/benchmark


ssh -i ~/.ssh/id_rsa  ec2-user@172.30.0.90 "cd ./benchmark; \
tar zxvf benchmark-suite.tar.gz;"

ssh -i ~/.ssh/id_rsa  ec2-user@172.30.0.205 "cd ./benchmark; \
tar zxvf benchmark-suite.tar.gz;"

ssh -i ~/.ssh/id_rsa  ec2-user@172.30.0.44 "cd ./benchmark; \
tar zxvf benchmark-suite.tar.gz;"


echo "*******************"
echo "Deploying to Server-01 ..."
echo "*******************"
ssh -i ~/.ssh/id_rsa  ec2-user@172.30.0.90 "cd ./benchmark/coherence/build/distributions; rm -rf lib jmi-lib; "
ssh -i ~/.ssh/id_rsa  ec2-user@172.30.0.90 "cd ./benchmark/geode/build/distributions; rm -rf lib jmi-lib; "
ssh -i ~/.ssh/id_rsa  ec2-user@172.30.0.90 "cd ./benchmark/hazelcast/build/distributions; rm -rf lib jmi-lib; "
ssh -i ~/.ssh/id_rsa  ec2-user@172.30.0.90 "cd ./benchmark/ignite/build/distributions; rm -rf lib jmi-lib; "

echo "*******************"
echo "Deploying to Server-02 ..."
echo "*******************"
ssh -i ~/.ssh/id_rsa  ec2-user@172.30.0.205 "cd ./benchmark/coherence/build/distributions; rm -rf lib jmi-lib; "
ssh -i ~/.ssh/id_rsa  ec2-user@172.30.0.205 "cd ./benchmark/geode/build/distributions; rm -rf lib jmi-lib; "
ssh -i ~/.ssh/id_rsa  ec2-user@172.30.0.205 "cd ./benchmark/hazelcast/build/distributions; rm -rf lib jmi-lib; "
ssh -i ~/.ssh/id_rsa  ec2-user@172.30.0.205 "cd ./benchmark/ignite/build/distributions; rm -rf lib jmi-lib; "

echo "*******************"
echo "Deploying to Server-03..."
echo "*******************"
ssh -i ~/.ssh/id_rsa  ec2-user@172.30.0.44 "cd ./benchmark/coherence/build/distributions; rm -rf lib jmi-lib; "
ssh -i ~/.ssh/id_rsa  ec2-user@172.30.0.44 "cd ./benchmark/geode/build/distributions; rm -rf lib jmi-lib; "
ssh -i ~/.ssh/id_rsa  ec2-user@172.30.0.44 "cd ./benchmark/hazelcast/build/distributions; rm -rf lib jmi-lib; "
ssh -i ~/.ssh/id_rsa  ec2-user@172.30.0.44 "cd ./benchmark/ignite/build/distributions; rm -rf lib jmi-lib; "

echo "*******************"
echo "Installing to Server-01..."
echo "*******************"
ssh -i ~/.ssh/id_rsa  ec2-user@172.30.0.90 "cd ./benchmark/coherence/build/distributions; \
tar xvf benchmark.coherence-1.0-SNAPSHOT.tar;"
ssh -i ~/.ssh/id_rsa  ec2-user@172.30.0.90 "cd ./benchmark/geode/build/distributions; \
tar xvf benchmark.geode-1.0-SNAPSHOT.tar;"
ssh -i ~/.ssh/id_rsa  ec2-user@172.30.0.90 "cd ./benchmark/hazelcast/build/distributions; \
tar xvf benchmark.hazelcast-1.0-SNAPSHOT.tar;"
ssh -i ~/.ssh/id_rsa  ec2-user@172.30.0.90 "cd ./benchmark/ignite/build/distributions; \
tar xvf benchmark.ignite-1.0-SNAPSHOT.tar;"

echo "*******************"
echo "Installing to Server-02..."
echo "*******************"
ssh -i ~/.ssh/id_rsa  ec2-user@172.30.0.205 "cd ./benchmark/coherence/build/distributions; \
tar xvf benchmark.coherence-1.0-SNAPSHOT.tar;"
ssh -i ~/.ssh/id_rsa  ec2-user@172.30.0.205 "cd ./benchmark/geode/build/distributions; \
tar xvf benchmark.geode-1.0-SNAPSHOT.tar;"
ssh -i ~/.ssh/id_rsa  ec2-user@172.30.0.205 "cd ./benchmark/hazelcast/build/distributions; \
tar xvf benchmark.hazelcast-1.0-SNAPSHOT.tar;"
ssh -i ~/.ssh/id_rsa  ec2-user@172.30.0.205 "cd ./benchmark/ignite/build/distributions; \
tar xvf benchmark.ignite-1.0-SNAPSHOT.tar;"

echo "*******************"
echo "Installing to Server-03 ..."
echo "*******************"
ssh -i ~/.ssh/id_rsa  ec2-user@172.30.0.44 "cd ./benchmark/coherence/build/distributions; \
tar xvf benchmark.coherence-1.0-SNAPSHOT.tar;"
ssh -i ~/.ssh/id_rsa  ec2-user@172.30.0.44 "cd ./benchmark/geode/build/distributions; \
tar xvf benchmark.geode-1.0-SNAPSHOT.tar;"
ssh -i ~/.ssh/id_rsa  ec2-user@172.30.0.44 "cd ./benchmark/hazelcast/build/distributions; \
tar xvf benchmark.hazelcast-1.0-SNAPSHOT.tar;"
ssh -i ~/.ssh/id_rsa  ec2-user@172.30.0.44 "cd ./benchmark/ignite/build/distributions; \
tar xvf benchmark.ignite-1.0-SNAPSHOT.tar;"


echo "*******************"
echo "Done!"
echo "*******************"
