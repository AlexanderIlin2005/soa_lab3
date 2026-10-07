package org.itmo.vehicle.infrastructure.persistence;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Flyway-миграции. Spring Boot сам вызовет {@code Flyway.migrate()}
 * при старте приложения, если бин Flyway есть в контексте.
 *
 * DataSource берётся Spring'ом из JNDI (spring.datasource.jndi-name
 * = java:comp/env/jdbc/vehicles), схема — из soa.db-schema.
 */
@Configuration(proxyBeanMethods = false)
public class SchemaMigration {

    @Bean
    Flyway flyway(DataSource dataSource,
                  @Value("${soa.db-schema}") String schema) {
        return Flyway.configure()
                .dataSource(dataSource)
                .schemas(schema)
                .table("soa_flyway_history")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .locations("classpath:db/migration")
                .load();
    }
}
