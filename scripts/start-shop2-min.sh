#!/usr/local/bin/bash
cd ~/soa-lab3

pkill -9 -f "payara-micro.jar.*8082" 2>/dev/null || true
sleep 3
rm -rf ~/tmp/payaramicro-rt* 2>/dev/null || true

ulimit -s 8192

JVM_SHOP2="-Xms32m -Xmx256m -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC -XX:-TieredCompilation -XX:ReservedCodeCacheSize=16m -XX:CICompilerCount=1 -Xss160k -Djava.io.tmpdir=$HOME/tmp -XX:-UsePerfData -Dotel.java.global-autoconfigure.enabled=false -Dmp.metrics.enabled=false -Dmp.health.enabled=false -Dmp.openapi.enabled=false -Dfish.payara.micro.noJsf=true -Dfish.payara.micro.noJmx=true -Dfish.payara.micro.noMetrics=true -Dfish.payara.micro.grizzly.corepoolsize=1 -Dfish.payara.micro.grizzly.maxpoolsize=4"

nohup bash -c "ulimit -s 8192 && _JAVA_OPTIONS='$JVM_SHOP2 -Dspring.profiles.active=helios' VEHICLE_SERVICE_URL='http://127.0.0.1:38080/vehicle-service' java -jar jars/payara-micro.jar --deploy wars/shop-service.war --port 8082 --nohazelcast --clustermode tcpip:127.0.0.1:16902 --contextroot shop-service" > logs/shop2.log 2>&1 &

echo ">>> shop2 PID: $!"
sleep 120

curl -s -o /dev/null -w "shop2: %{http_code}\n" http://127.0.0.1:8082/shop-service/shop/search/by-type/CHOPPER
tail -20 logs/shop2.log
df -h ~ | tail -1
