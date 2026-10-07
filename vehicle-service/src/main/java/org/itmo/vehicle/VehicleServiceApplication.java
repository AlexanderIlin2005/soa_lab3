package org.itmo.vehicle;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Точка входа. WAR деплоится в Payara в контекст /vehicle-service.
 * SpringBootServletInitializer обязателен для запуска внутри внешнего
 * контейнера сервлетов (Payara). Клиент Eureka активируется
 * автоконфигурацией при наличии стартера в classpath.
 */
@SpringBootApplication
public class VehicleServiceApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(VehicleServiceApplication.class);
    }
}
