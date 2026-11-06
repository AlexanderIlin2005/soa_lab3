package org.itmo.shop.infrastructure.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import org.itmo.shop.application.ShopService;
import org.itmo.shop.domain.VehicleCatalog;
import org.itmo.shop.infrastructure.vehicleservice.HttpVehicleCatalog;

import java.time.Duration;

@ApplicationScoped
public class ApplicationWiring {

    private static final String DEFAULT_VEHICLE_SERVICE_URL = "http://127.0.0.1:8092/vehicle-service";

    @Produces
    @Singleton
    VehicleCatalog vehicleCatalog() {
        String url = System.getProperty("vehicle-service.base-url",
                System.getenv().getOrDefault("VEHICLE_SERVICE_URL", DEFAULT_VEHICLE_SERVICE_URL));
        return new HttpVehicleCatalog(url, Duration.ofSeconds(2), Duration.ofSeconds(5));
    }

    @Produces
    @Singleton
    ShopService shopService(VehicleCatalog vehicles) {
        return new ShopService(vehicles);
    }
}
