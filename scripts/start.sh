#!/bin/bash
PID=$(pgrep -f rook.daemon.Daemon)
if [[ -z $PID ]]; then
  echo "Starting Rook Daemon"  
else
  echo "Already Running"
  exit
fi

nohup java -cp "platform/daemon/java/*" \
  rook.daemon.Daemon \
  8080 \
  &> daemon.log &