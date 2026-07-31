#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
NAME="$(basename "$ROOT")"
STAMP="$(date +%Y%m%d-%H%M%S)"
OUT_DIR="${SHARE_OUT_DIR:-$ROOT/../}"
OUT_ZIP="${OUT_DIR%/}/${NAME}-share-${STAMP}.zip"

cd "$ROOT"

# 排除编译产物、依赖、运行时数据与系统垃圾
zip -r "$OUT_ZIP" . \
  -x './.git/*' \
  -x './.git' \
  -x './.idea/*' \
  -x './.vscode/*' \
  -x './.DS_Store' \
  -x '*/.DS_Store' \
  -x './backend/.gradle/*' \
  -x './backend/build/*' \
  -x './backend/bin/*' \
  -x './backend/data/*' \
  -x './backend/out/*' \
  -x './backend/src/main/resources/static/*' \
  -x './frontend/node_modules/*' \
  -x './frontend/dist/*' \
  -x './frontend/.vite/*' \
  -x './dist/*' \
  -x './*.log' \
  -x './*/*.log' \
  -x './*.iml' \
  -x './*/*.iml' \
  -x './*-share-*.zip' \
  -x './scripts/*-share-*.zip'

echo "==> 已生成: $OUT_ZIP"
ls -lh "$OUT_ZIP"
