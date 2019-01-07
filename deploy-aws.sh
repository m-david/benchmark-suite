#!/usr/bin/env bash

scp -i ~/.ssh/manny-d-01.pem -o StrictHostKeyChecking=no ./build/benchmark-suite.tar.gz ec2-user@35.175.173.52:/home/ec2-user/benchmark

