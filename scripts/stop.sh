#!/bin/bash
PID=$(pgrep -f io.septapulse.rook.daemon.Daemon)
if [[ -z $PID ]]; then
  echo "Nothing to kill"  
else
  echo "Killing PID ${PID}"
  kill ${PID}
fi