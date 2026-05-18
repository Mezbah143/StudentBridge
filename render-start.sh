#!/bin/sh
set -eu

PORT_VALUE="${PORT:-8080}"
SERVER_XML="${CATALINA_HOME}/conf/server.xml"

sed -i.bak "0,/port=\"8080\"/s//port=\"${PORT_VALUE}\"/" "$SERVER_XML"

exec catalina.sh run
