#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
DIST="$ROOT/dist/kids-learn-8050"
JAR_NAME="kids-learn.jar"

echo "==> 构建前端"
cd "$ROOT/frontend"
npm install --silent
npm run build

echo "==> 拷贝前端到后端 static"
rm -rf "$ROOT/backend/src/main/resources/static"
mkdir -p "$ROOT/backend/src/main/resources/static"
cp -R "$ROOT/frontend/dist/." "$ROOT/backend/src/main/resources/static/"

echo "==> 打包 Spring Boot Jar"
cd "$ROOT/backend"
./gradlew clean bootJar -q

JAR_SRC="$(ls -1 "$ROOT/backend/build/libs"/*SNAPSHOT.jar | head -n 1)"
rm -rf "$DIST"
mkdir -p "$DIST/data/audio"

echo "==> 拆分依赖到 lib/，核心输出为薄 jar"
# Spring Boot tools：业务/静态资源 → kids-learn.jar；第三方依赖 → lib/
java -Djarmode=tools -jar "$JAR_SRC" extract \
  --destination "$DIST" \
  --libraries lib \
  --application-filename "$JAR_NAME" \
  --force

# 可选：保留完整胖包，方便只要单文件的场景（默认不拷贝，节省体积）
# cp "$JAR_SRC" "$DIST/kids-learn-all.jar"

cp "$ROOT/scripts/start.sh" "$DIST/start.sh"
cp "$ROOT/scripts/stop.sh" "$DIST/stop.sh"
cp "$ROOT/部署说明-1Panel.md" "$DIST/部署说明-1Panel.md"
chmod +x "$DIST/start.sh" "$DIST/stop.sh"

# 可选：带上本地已有库与发音缓存，减少首启等待
if [[ -f "$ROOT/backend/data/kidslearn.db" ]]; then
  cp "$ROOT/backend/data/kidslearn.db" "$DIST/data/kidslearn.db"
fi
if [[ -d "$ROOT/backend/data/audio" ]]; then
  find "$ROOT/backend/data/audio" -name '*.mp3' -maxdepth 1 | head -n 300 | while read -r f; do
    cp "$f" "$DIST/data/audio/" 2>/dev/null || true
  done
fi

echo "==> 完成"
echo "输出目录: $DIST"
echo "核心包: $(du -h "$DIST/$JAR_NAME" | awk '{print $1}')  （日常更新只传这个）"
echo "依赖目录: $(du -sh "$DIST/lib" | awk '{print $1}')  （依赖不变时可跳过）"
ls -la "$DIST"
