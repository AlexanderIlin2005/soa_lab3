#!/bin/bash
set -euo pipefail

PUBLIC_HOST="${1:?usage: install.sh <public host or IP>}"
HERE="$(cd "$(dirname "$0")" && pwd)"
APP=/opt/soa

if ! swapon --show | grep -q /swapfile; then
  fallocate -l 2G /swapfile && chmod 600 /swapfile && mkswap /swapfile >/dev/null && swapon /swapfile
fi

export DEBIAN_FRONTEND=noninteractive
apt-get update -qq
apt-get install -y -qq --no-install-recommends openjdk-17-jdk-headless postgresql haproxy curl openssl >/dev/null

id soa >/dev/null 2>&1 || useradd --system --create-home --home-dir "$APP" --shell /bin/bash soa
install -d -o soa -g soa "$APP/logs" "$APP/tmp" "$APP/jars" "$APP/wars"
install -d -o soa -g soa -m 700 "$APP/secrets"
install -o soa -g soa -m 644 "$HERE"/jars/*.jar "$APP/jars/"
install -o soa -g soa -m 644 "$HERE"/wars/*.war "$APP/wars/"
install -o soa -g soa -m 755 "$HERE/start.sh" "$HERE/stop.sh" "$HERE/seed.sh" "$APP/"
install -o soa -g soa -m 644 "$HERE/payara-preboot.asadmin" "$APP/"

if [ ! -d "$APP/wildfly" ]; then
  tar -xzf "$HERE"/dist/wildfly-*.tar.gz -C "$APP" && mv "$APP"/wildfly-* "$APP/wildfly"
  "$APP/wildfly/bin/jboss-cli.sh" --file="$HERE/wildfly.cli" >/dev/null
fi
if [ ! -d "$APP/mule" ]; then
  tar -xzf "$HERE"/dist/mule-standalone-*.tar.gz -C "$APP" && mv "$APP"/mule-standalone-* "$APP/mule"
fi
cp "$HERE"/wars/shop-soap.war "$HERE"/wars/client.war "$HERE"/wars/swagger.war "$APP/wildfly/standalone/deployments/"
cp "$HERE"/esb/shop-esb-*.jar "$APP/mule/apps/"
chown -R soa:soa "$APP"

if [ ! -s "$APP/secrets/db.env" ]; then
  PW=$(openssl rand -hex 16)
  su postgres -c "psql -qAt -v pw='$PW'" <<'SQL'
SELECT 'CREATE ROLE vehicle_app LOGIN' WHERE NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'vehicle_app') \gexec
ALTER ROLE vehicle_app PASSWORD :'pw';
SELECT 'CREATE DATABASE vehicle_db OWNER vehicle_app' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'vehicle_db') \gexec
REVOKE CONNECT ON DATABASE vehicle_db FROM PUBLIC;
SQL
  install -o soa -g soa -m 600 /dev/null "$APP/secrets/db.env"
  printf 'DB_PASSWORD=%s\n' "$PW" > "$APP/secrets/db.env"
fi
printf 'PUBLIC_HOST=%s\n' "$PUBLIC_HOST" > "$APP/secrets/host.env"
chown soa:soa "$APP/secrets/host.env"

if [ ! -s /etc/haproxy/soa.pem ]; then
  SAN="DNS:localhost,IP:127.0.0.1"
  if [[ "$PUBLIC_HOST" =~ ^[0-9.]+$ ]]; then SAN="$SAN,IP:$PUBLIC_HOST"; else SAN="$SAN,DNS:$PUBLIC_HOST"; fi
  (umask 077 && openssl req -x509 -newkey rsa:2048 -nodes -days 30 -subj "/CN=$PUBLIC_HOST" -addext "subjectAltName=$SAN" \
    -keyout /tmp/soa.key -out /tmp/soa.crt 2>/dev/null && cat /tmp/soa.crt /tmp/soa.key > /etc/haproxy/soa.pem)
  rm -f /tmp/soa.key /tmp/soa.crt
fi
install -m 644 "$HERE/haproxy.cfg" /etc/haproxy/haproxy.cfg
haproxy -c -q -f /etc/haproxy/haproxy.cfg
systemctl enable --now haproxy >/dev/null 2>&1
systemctl restart haproxy

echo "installed; now: su - soa -c $APP/start.sh"
