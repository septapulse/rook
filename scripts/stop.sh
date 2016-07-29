#!/bin/bash
PID=$(ps -ef | grep java | grep UILauncher | tr -s ' ' | cut -d ' ' -f 2)
if [[ -z $PID ]]; then
  echo "Nothing to kill"  
else
  echo "Killing PID ${PID}"
  kill ${PID}
fi