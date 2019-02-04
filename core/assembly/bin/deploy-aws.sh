#!/usr/bin/env bash

if [ "x$AWS_CLIENT_SERVER" = "x" ]; then
    echo "Please set env variable AWS_CLIENT_SERVER first!"
else
    scp -i ~/.ssh/manny-d-01.pem -o StrictHostKeyChecking=no ./build/benchmark-suite.tar.gz ec2-user@${AWS_CLIENT_SERVER}:/home/ec2-user/benchmark
fi
