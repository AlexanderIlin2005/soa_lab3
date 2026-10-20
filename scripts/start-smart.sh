#!/usr/local/bin/bash
cd ~/soa-lab3

echo ">>> 1. Убиваем ВСЁ java и haproxy"
pkill -9 -f "payara-micro.jar" 2>/dev/null || true
pkill -9 -f "eureka-server.jar" 2>/dev/null || true
pkill -9 -f "config-server.jar" 2>/dev/null || true
pkill -9 -f "api-gateway.jar" 2>/dev/null || true
pkill -9 -f "haproxy" 2>/dev/null || true
sleep 5

echo ">>> 2. Чистим /tmp и ~/tmp"
rm -rf /tmp/payaramicro-rt* /tmp/hsperfdata_* 2>/dev/null
rm -rf ~/tmp/payaramicro-rt* 2>/dev/null
df -h /tmp | tail -1

ulimit -s 8192
echo ">>> ulimit -s = $(ulimit -s)"

# Базовые флаги: отключаем JMX, OTEL, метрики Payara, ограничиваем Grizzly
COMMON_PAYARA_OPTS="-Djava.io.tmpdir=$HOME/tmp \
  -XX:-UsePerfData \
  -Dotel.java.global-autoconfigure.enabled=false \
  -Dmp.metrics.enabled=false \
  -Dmp.health.enabled=false \
  -Dmp.openapi.enabled=false \
  -Dfish.payara.micro.grizzly.corepoolsize=2 \
  -Dfish.payara.micro.grizzly.maxpoolsize=8"

JVM_MED="-Xms64m -Xmx384m -XX:MaxMetaspaceSize=192m -XX:+UseSerialGC -XX:-TieredCompilation -XX:ReservedCodeCacheSize=24m -XX:CICompilerCount=1 -Xss192k"
JVM_TINY="-Xms32m -Xmx128m -XX:MaxMetaspaceSize=64m -XX:+UseSerialGC -XX:-TieredCompilation -XX:ReservedCodeCacheSize=24m -XX:CICompilerCount=1 -Xss192k"

# --- 3. VEHICLE ---
echo
echo ">>> 3. Запуск VEHICLE (8080)"
nohup bash -c "ulimit -s 8192 && _JAVA_OPTIONS='$JVM_MED $COMMON_PAYARA_OPTS -Dspring.profiles.active=helios' java -jar jars/payara-micro.jar --deploy wars/vehicle-service.war --port 8080 --nohazelcast --clustermode tcpip:127.0.0.1:16900 --contextroot vehicle-service" > logs/vehicle.log 2>&1 &
echo "Ждём 120 сек..."
sleep 120
VCODE=$(curl -s -o /dev/null -w '%{http_code}' http://127.0.0.1:8080/vehicle-service/vehicles || echo "000")
echo "vehicle: $VCODE"
[ "$VCODE" = "200" ] || { echo "❌ vehicle упал"; tail -20 logs/vehicle.log; exit 1; }

# --- 4. SHOP1 ---
echo
echo ">>> 4. Запуск SHOP1 (8081)"
nohup bash -c "ulimit -s 8192 && _JAVA_OPTIONS='$JVM_MED $COMMON_PAYARA_OPTS -Dspring.profiles.active=helios' VEHICLE_SERVICE_URL='http://127.0.0.1:38080/vehicle-service' java -jar jars/payara-micro.jar --deploy wars/shop-service.war --port 8081 --nohazelcast --clustermode tcpip:127.0.0.1:16901 --contextroot shop-service" > logs/shop1.log 2>&1 &
echo "Ждём 90 сек..."
sleep 90
S1CODE=$(curl -s -o /dev/null -w '%{http_code}' http://127.0.0.1:8081/shop-service/shop/search/by-type/CHOPPER || echo "000")
echo "shop1: $S1CODE"
[ "$S1CODE" != "000" ] || { echo "❌ shop1 упал"; tail -20 logs/shop1.log; exit 1; }

# --- 5. GATEWAY ---
echo
echo ">>> 5. Запуск GATEWAY (38080)"
nohup bash -c "ulimit -s 8192 && _JAVA_OPTIONS='$JVM_TINY -Djava.io.tmpdir=$HOME/tmp -XX:-UsePerfData -Dotel.java.global-autoconfigure.enabled=false -Dspring.profiles.active=helios' java -jar jars/api-gateway.jar" > logs/gateway.log 2>&1 &
echo "Ждём 40 сек..."
sleep 40
GWCODE=$(curl -s -o /dev/null -w '%{http_code}' http://127.0.0.1:38080/vehicle-service/vehicles || echo "000")
echo "gateway→vehicle: $GWCODE"
[ "$GWCODE" = "200" ] || { echo "❌ gateway упал"; tail -20 logs/gateway.log; exit 1; }

# --- 6. EUREKA ---
echo
echo ">>> 6. Запуск EUREKA (38761)"
nohup bash -c "ulimit -s 8192 && _JAVA_OPTIONS='$JVM_TINY -Djava.io.tmpdir=$HOME/tmp -XX:-UsePerfData -Dotel.java.global-autoconfigure.enabled=false -Dspring.profiles.active=helios' java -jar jars/eureka-server.jar" > logs/eureka.log 2>&1 &
echo "Ждём 30 сек..."
sleep 30
ECODE=$(curl -s -o /dev/null -w '%{http_code}' http://127.0.0.1:38761/ || echo "000")
echo "eureka: $ECODE"

# --- 7. CONFIG ---
echo
echo ">>> 7. Запуск CONFIG (38888)"
nohup bash -c "ulimit -s 8192 && _JAVA_OPTIONS='$JVM_TINY -Djava.io.tmpdir=$HOME/tmp -XX:-UsePerfData -Dotel.java.global-autoconfigure.enabled=false -Dspring.profiles.active=native,helios' java -jar jars/config-server.jar" > logs/config.log 2>&1 &
echo "Ждём 30 сек..."
sleep 30
CCODE=$(curl -s -o /dev/null -w '%{http_code}' http://127.0.0.1:38888/vehicle-service/default || echo "000")
echo "config: $CCODE"

# --- 8. HAProxy ---
echo
echo ">>> 8. Запуск HAProxy"
pkill -9 -f "haproxy" 2>/dev/null || true
sleep 2
nohup ~/tools/bin/haproxy -f haproxy.cfg > logs/haproxy.log 2>&1 &
sleep 10

echo
echo "========== SMOKE-TEST =========="
echo -n "eureka:           "; curl -s -o /dev/null -w "%{http_code}\n" http://127.0.0.1:38761/
echo -n "config:           "; curl -s -o /dev/null -w "%{http_code}\n" http://127.0.0.1:38888/vehicle-service/default
echo -n "vehicle:          "; curl -s -o /dev/null -w "%{http_code}\n" http://127.0.0.1:8080/vehicle-service/vehicles
echo -n "shop1:            "; curl -s -o /dev/null -w "%{http_code}\n" http://127.0.0.1:8081/shop-service/shop/search/by-type/CHOPPER
echo -n "gateway→vehicle:  "; curl -s -o /dev/null -w "%{http_code}\n" http://127.0.0.1:38080/vehicle-service/vehicles
echo -n "https vehicle:    "; curl -sk -o /dev/null -w "%{http_code}\n" https://127.0.0.1:28581/vehicle-service/vehicles
echo -n "https shop:       "; curl -sk -o /dev/null -w "%{http_code}\n" https://127.0.0.1:28681/shop-service/shop/search/by-type/CHOPPER
echo
echo "--- HAProxy stats ---"
curl -s http://127.0.0.1:8404/ | grep -oE 'class="active_(up|down)"' | sort | uniq -c
echo
echo "--- /tmp ---"
df -h /tmp | tail -1
echo
echo ">>> ГОТОВО"
