#!/usr/bin/env bash

PRG="$0"
PRGDIR=`dirname "$PRG"`
APP_HOME=`cd "$PRGDIR/.." >/dev/null; pwd`

gfsh -e "connect --locator=10.212.1.116[10680]" -e "shutdown --include-locators=true"
