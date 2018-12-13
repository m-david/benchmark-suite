#!/usr/bin/env bash

scp -i ~/.ssh/id_rsa -o StrictHostKeyChecking=no ./build/benchmark-suite.tar.gz manny.david@10.212.1.116:/home/manny.david/benchmark

