package org.itmo.vehicle.infrastructure.web;

import org.itmo.vehicle.application.VehicleService;
import org.itmo.vehicle.infrastructure.web.generated.api.StatisticsApi;
import org.itmo.vehicle.infrastructure.web.generated.model.EnginePowerSumDto;
import org.itmo.vehicle.infrastructure.web.generated.model.NumberOfWheelsAverageDto;
import org.itmo.vehicle.infrastructure.web.generated.model.VehicleDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Реализация StatisticsApi. Пути, методы, коды — из openapi/vehicle-service.yaml.
 */
@RestController
public class StatisticsResource implements StatisticsApi {

    private final VehicleService service;
    private final VehicleDtoMapper mapper;

    public StatisticsResource(VehicleService service, VehicleDtoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @Override
    public ResponseEntity<EnginePowerSumDto> getEnginePowerSum() {
        return ResponseEntity.ok(mapper.toDto(service.enginePowerStatistics()));
    }

    @Override
    public ResponseEntity<NumberOfWheelsAverageDto> getNumberOfWheelsAverage() {
        return ResponseEntity.ok(mapper.toDto(service.wheelsStatistics()));
    }

    @Override
    public ResponseEntity<VehicleDto> getVehicleWithMaxName() {
        return ResponseEntity.ok(mapper.toDto(service.vehicleWithMaxName()));
    }
}
