#!/bin/bash
set -euo pipefail

DIST="${1:?usage: bundle.sh <folder with wildfly-*.tar.gz and mule-standalone-*.tar.gz>}"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="$ROOT/vps/build/bundle"
export JAVA_HOME="${JAVA_HOME:-$(/usr/libexec/java_home -v 17)}"
MVN="${MVN:-mvn}"

(cd "$ROOT" && ./gradlew -q assemble)
(cd "$ROOT/esb" && "$MVN" -q -B package -DskipTests)

rm -rf "$OUT" && mkdir -p "$OUT/jars" "$OUT/wars" "$OUT/esb" "$OUT/dist"
cp "$ROOT"/vps/{install.sh,start.sh,stop.sh,seed.sh,haproxy.cfg,payara-preboot.asadmin,wildfly.cli} "$OUT/"
cp "$ROOT/docker/payara-micro/payara-micro.jar" "$OUT/jars/"
for m in eureka-server config-server vehicle-registrar api-gateway; do
  cp "$ROOT/lab3-cloud/$m/build/libs/$m.jar" "$OUT/jars/"
done
cp "$ROOT/vehicle-service/build/libs/vehicle-service.war" "$ROOT/shop-service/build/libs/shop-soap.war" \
   "$ROOT/web-client/build/libs/client.war" "$ROOT/web-client/build/libs/swagger.war" "$OUT/wars/"
cp "$ROOT"/esb/target/shop-esb-*-mule-application.jar "$OUT/esb/"
cp "$DIST"/wildfly-*.tar.gz "$DIST"/mule-standalone-*.tar.gz "$OUT/dist/"
du -sh "$OUT"
