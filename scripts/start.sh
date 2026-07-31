#!/usr/bin/env bash
set -euo pipefail

APP_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$APP_DIR"

mkdir -p data/audio
PID_FILE="$APP_DIR/app.pid"
LOG_FILE="$APP_DIR/app.log"
JAR="$APP_DIR/kids-learn.jar"

JAVA_BIN="${JAVA_HOME:+$JAVA_HOME/bin/}java"
if ! command -v "${JAVA_BIN}" >/dev/null 2>&1; then
  JAVA_BIN="java"
fi

if [[ -f "$PID_FILE" ]] && kill -0 "$(cat "$PID_FILE")" 2>/dev/null; then
  echo "已在运行, pid=$(cat "$PID_FILE")"
  exit 0
fi

nohup "$JAVA_BIN" -Xms256m -Xmx512m \
  -Dserver.port=8050 \
  -jar "$JAR" \
  >"$LOG_FILE" 2>&1 &

echo $! >"$PID_FILE"
echo "已启动 pid=$(cat "$PID_FILE") 端口=8050"
echo "日志: $LOG_FILE"
