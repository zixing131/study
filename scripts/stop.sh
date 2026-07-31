#!/usr/bin/env bash
set -euo pipefail

APP_DIR="$(cd "$(dirname "$0")" && pwd)"
PID_FILE="$APP_DIR/app.pid"

if [[ ! -f "$PID_FILE" ]]; then
  echo "未找到 pid 文件"
  exit 0
fi

PID="$(cat "$PID_FILE")"
if kill -0 "$PID" 2>/dev/null; then
  kill "$PID"
  sleep 1
  if kill -0 "$PID" 2>/dev/null; then
    kill -9 "$PID" 2>/dev/null || true
  fi
  echo "已停止 pid=$PID"
else
  echo "进程不存在 pid=$PID"
fi
rm -f "$PID_FILE"
