#!/usr/local/bin/bash
cd ~

mkdir -p ~/logs ~/tmp

echo ">>> Убиваем java"
pkill -9 -f "eureka-server.jar" 2>/dev/null || true
pkill -9 -f "config-server.jar" 2>/dev/null || true
sleep 3

ulimit -s 8192

JVM_TINY="-Xms32m -Xmx128m -XX:MaxMetaspaceSize=64m -XX:+UseSerialGC -XX:-TieredCompilation -XX:ReservedCodeCacheSize=24m -XX:CICompilerCount=1 -Xss192k -Djava.io.tmpdir=$HOME/tmp -XX:-UsePerfData -Dotel.java.global-autoconfigure.enabled=false"

echo ">>> EUREKA (38761)"
cd ~/eureka
nohup bash -c "ulimit -s 8192 && _JAVA_OPTIONS='$JVM_TINY -Dspring.profiles.active=helios' java -jar eureka-server.jar" > ~/logs/eureka.log 2>&1 &
sleep 30
echo -n "eureka: "; curl -s -o /dev/null -w "%{http_code}\n" http://127.0.0.1:38761/

echo ">>> CONFIG (38888)"
cd ~/config
nohup bash -c "ulimit -s 8192 && _JAVA_OPTIONS='$JVM_TINY -Dspring.profiles.active=native,helios' java -jar config-server.jar" > ~/logs/config.log 2>&1 &
sleep 30
echo -n "config: "; curl -s -o /dev/null -w "%{http_code}\n" http://127.0.0.1:38888/vehicle-service/default

echo ">>> ГОТОВО"
