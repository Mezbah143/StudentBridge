#!/bin/sh
set -eu

PORT_VALUE="${PORT:-8080}"
SERVER_XML="${CATALINA_HOME}/conf/server.xml"
RUNTIME_CONFIG="${CATALINA_HOME}/webapps/ROOT/frontend/runtime-config.js"

js_escape() {
  printf '%s' "$1" | sed "s/\\\\/\\\\\\\\/g; s/'/\\\\'/g"
}

BACKEND_BASE="$(js_escape "${STUDENTBRIDGE_BACKEND_BASE_URL:-}")"
KAKAO_MAP_KEY="$(js_escape "${STUDENTBRIDGE_KAKAO_MAP_KEY:-}")"

cat > "$RUNTIME_CONFIG" <<EOF
window.STUDENTBRIDGE_BACKEND_BASE_URL = window.STUDENTBRIDGE_BACKEND_BASE_URL || '${BACKEND_BASE}';
window.STUDENTBRIDGE_KAKAO_MAP_KEY = window.STUDENTBRIDGE_KAKAO_MAP_KEY || '${KAKAO_MAP_KEY}';
EOF

sed -i.bak "0,/port=\"8080\"/s//port=\"${PORT_VALUE}\"/" "$SERVER_XML"

exec catalina.sh run
