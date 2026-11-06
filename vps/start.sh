#!/bin/bash
APP=/opt/soa
cd "$APP" || exit 1
set -a
. "$APP/secrets/db.env"
. "$APP/secrets/host.env"
set +a

code() { curl -sk -o /dev/null -m 10 -w '%{http_code}' "$1"; }

wait_for() {
  local t=0
  until [ "$(code "$2")" = "$3" ]; do
    if [ "$t" -ge 300 ]; then echo "FAIL $1 ($2)"; tail -20 "logs/$1.log"; exit 1; fi
    sleep 5; t=$((t + 5))
  done
  echo "ok   $1 (${t}s)"
}

boot() {
  nohup java "-Xmx$3" -Djava.io.tmpdir="$APP/tmp" -jar "jars/$1.jar" --spring.profiles.active="$2" \
    --server.address=127.0.0.1 > "logs/$1.log" 2>&1 &
}

boot eureka-server helios 128m
wait_for eureka-server http://127.0.0.1:38761/ 200
boot config-server native,helios 128m
boot vehicle-registrar helios 128m
boot api-gateway helios 192m

rm -rf "$APP/tmp/vehicle" && mkdir -p "$APP/tmp/vehicle"
SPRING_DATASOURCE_URL="jdbc:postgresql://127.0.0.1:5432/vehicle_db?currentSchema=soa" SPRING_DATASOURCE_USERNAME=vehicle_app \
SPRING_DATASOURCE_PASSWORD="$DB_PASSWORD" \
  nohup java -Xmx384m -Djava.io.tmpdir="$APP/tmp/vehicle" -Dsoa.cors.allowed-origins="https://$PUBLIC_HOST:28681" \
    -jar jars/payara-micro.jar --prebootcommandfile payara-preboot.asadmin \
    --deploy wars/vehicle-service.war --contextroot vehicle-service --port 8080 --nohazelcast \
    > logs/vehicle-service.log 2>&1 &

nohup wildfly/bin/standalone.sh -Djboss.socket.binding.port-offset=100 \
  -Dvehicle-service.base-url=http://127.0.0.1:8092/vehicle-service > logs/wildfly.log 2>&1 &

mule/bin/mule start > logs/mule-start.log 2>&1

wait_for config-server http://127.0.0.1:38888/vehicle-service/default 200
wait_for vehicle-service http://127.0.0.1:8080/vehicle-service/vehicles 200
wait_for api-gateway http://127.0.0.1:38080/vehicle-service/vehicles 200
wait_for wildfly "http://127.0.0.1:8180/shop-soap/ShopService?wsdl" 200
wait_for mule http://127.0.0.1:8091/shop-service/shop/search/by-type/CHOPPER 200
wait_for haproxy "https://127.0.0.1:28681/shop-service/shop/search/by-type/CHOPPER" 200

echo
echo "client:  https://$PUBLIC_HOST:28681/client/"
echo "swagger: https://$PUBLIC_HOST:28681/swagger/"
echo "wsdl:    https://$PUBLIC_HOST:28681/shop-soap/ShopService?wsdl"
echo "eureka:  ssh -L 38761:127.0.0.1:38761, then http://localhost:38761"
