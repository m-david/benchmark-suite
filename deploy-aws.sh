#!/usr/bin/env bash

scp -i ~/.ssh/manny-d-01.pem -o StrictHostKeyChecking=no ./build/benchmark-suite.tar.gz ec2-user@52.90.99.172:/home/ec2-user/benchmark

