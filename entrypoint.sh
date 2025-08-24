#!/bin/bash

PUID=${PUID:-1000}
PGID=${PGID:-1000}

if ! getent group "$PGID" > /dev/null 2>&1; then
    groupadd -g "$PGID" appgroup
fi

if ! getent passwd "$PUID" > /dev/null 2>&1; then
    useradd -u "$PUID" -g "$PGID" -d /workspace -s /bin/bash appuser
fi

USERNAME=$(getent passwd "$PUID" | cut -d: -f1)

mkdir -p /workspace/data
chown -R "$PUID:$PGID" /workspace

exec gosu "$PUID:$PGID" /usr/local/bin/gitsheets "$@"