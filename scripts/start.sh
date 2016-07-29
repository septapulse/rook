#!/bin/bash
PID=$(ps -ef | grep java | grep UILauncher | tr -s ' ' | cut -d ' ' -f 2)
if [[ -z $PID ]]; then
  echo "Starting Rook"  
else
  echo "Already Running"
  exit
fi

nohup java -cp "platform/ui/lib/*:platform/api/*" rook.ui.UILauncher \
  --router-type rook.api.transport.tcp.TcpRouter \
  --router-config "{}" \
  --router-package "tcp" \
  --transport-type rook.api.transport.tcp.TcpTransport \
  --transport-config "{}" \
  --html platform/ui/html \
  --port 9000 \
  &> ui.log &