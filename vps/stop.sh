#!/bin/bash
APP=/opt/soa
JVMS='jboss-modules.jar|payara-micro.jar|jars/(eureka-server|config-server|vehicle-registrar|api-gateway).jar'

"$APP/mule/bin/mule" stop >/dev/null 2>&1
pkill -f "$JVMS"
for _ in $(seq 1 30); do
  pgrep -f "$JVMS" >/dev/null || break
  sleep 1
done
pkill -9 -f "$JVMS" 2>/dev/null
echo stopped
