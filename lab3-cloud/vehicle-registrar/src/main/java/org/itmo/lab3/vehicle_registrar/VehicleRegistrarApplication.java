package org.itmo.lab3.vehicle_registrar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Sidecar-сервис. Не обслуживает запросы.
 * Единственная задача — зарегистрировать в Eureka инстанс VEHICLE-SERVICE,
 * указывающий на реальный vehicle-service (в Payara Micro).
 *
 * Зачем sidecar: vehicle-service — WAR на Payara Micro, и прямое подключение
 * spring-cloud-starter-netflix-eureka-client валит Weld с WELD-001408
 * (Netflix-классы @Inject-конструкторов). Регистрация отдельным процессом
 * обходит эту проблему.
 */
@SpringBootApplication
public class VehicleRegistrarApplication {
    public static void main(String[] args) {
        SpringApplication.run(VehicleRegistrarApplication.class, args);
    }
}
