package org.itmo.vehicle.infrastructure.config;

import org.itmo.vehicle.application.TransactionRunner;
import org.itmo.vehicle.application.VehicleService;
import org.itmo.vehicle.domain.VehicleRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Spring-конфигурация доменных сервисов. Заменяет CDI {@code @Produces}.
 * VehicleService, VehicleRepository и TransactionRunner — фреймворк-
 * агностичные классы, поэтому оборачиваем их в {@code @Bean} вручную.
 */
@Configuration(proxyBeanMethods = false)
public class ApplicationWiring {

    @Bean
    Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    VehicleService vehicleService(VehicleRepository vehicles, TransactionRunner transactions, Clock clock) {
        return new VehicleService(vehicles, transactions, clock);
    }
}
