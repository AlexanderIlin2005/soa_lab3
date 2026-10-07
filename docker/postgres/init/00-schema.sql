-- Схема для soa_vehicle и soa_flyway_history.
-- В локальном Postgres создаём её заранее, потому что у пользователя soa
-- есть права CREATE, но лучше явно.
CREATE SCHEMA IF NOT EXISTS soa AUTHORIZATION soa;