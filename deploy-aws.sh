#!/usr/bin/env bash

scp -i ~/.ssh/manny-d-01.pem -o StrictHostKeyChecking=no ./build/benchmark-suite.tar.gz ec2-user@ec2-34-229-174-95.compute-1.amazonaws.com:/home/ec2-user/benchmark

