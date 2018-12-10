#!/usr/bin/env bash

PRG="$0"
PRGDIR=`dirname "$PRG"`
APP_HOME=`cd "$PRGDIR/.." >/dev/null; pwd`

gfsh start locator --dir=$APP_HOME/locator1 --port=10680 --name=locator1

