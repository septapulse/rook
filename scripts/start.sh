#!/bin/bash
nohup java -cp "platform/ui/lib/*:platform/api/*" rook.ui.UILauncher \
  --router-type rook.api.transport.tcp.TcpRouter \
  --router-config "{}" \
  --router-package "tcp" \
  --transport-type rook.api.transport.tcp.TcpTransport \
  --transport-config "{}" \
  --html platform/ui/html \
  --port 9000 \
  &> ui.log &
