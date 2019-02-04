#!/usr/bin/env bash

echo "Stopping java process ..."
kill -9 $(ps aux | grep ec2-user | grep java | grep -v grep | awk '{print $2}');
echo "Stopped java processes."
