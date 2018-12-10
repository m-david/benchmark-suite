#!/usr/bin/env bash

PRG="$0"
PRGDIR=`dirname "$PRG"`
APP_HOME=`cd "$PRGDIR/.." >/dev/null; pwd`

WORK_DIRECTORY="$APP_HOME/server1/logs"
if [ ! -d "$WORK_DIRECTORY" ]; then
  mkdir -p "$WORK_DIRECTORY"
fi

export CLASS_PATH=\
$APP_HOME/conf:\
$APP_HOME/benchmark.ignite-1.0-SNAPSHOT.jar:\
$APP_HOME/lib/*

export IGNITE_LIBS=$CLASS_PATH