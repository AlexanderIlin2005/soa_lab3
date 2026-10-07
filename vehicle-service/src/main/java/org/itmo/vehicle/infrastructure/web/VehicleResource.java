package org.itmo.vehicle.infrastructure.web;

import org.itmo.vehicle.application.VehicleService;
import org.itmo.vehicle.domain.Vehicle;
import org.itmo.vehicle.infrastructure.web.generated.api.VehiclesApi;
import org.itmo.vehicle.infrastructure.web.generated.model.VehicleCreateRequestDto;
import org.itmo.vehicle.infrastructure.web.generated.model.VehicleDto;
import org.itmo.vehicle.infrastructure.web.generated.model.VehiclePageDto;
import org.itmo.vehicle.infrastructure.web.generated.model.VehicleUpdateRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * Реализация VehiclesApi. Пути, методы, коды ответов — из
 * openapi/vehicle-service.yaml. Генерируемый интерфейс тот же по
 * смыслу, что и раньше (только собран spring-генератором).
 */
@RestController
public class VehicleResource implements VehiclesApi {

    private final VehicleService service;
    private final VehicleQueryParser queryParser;
    private final VehicleDtoMapper mapper;

    public VehicleResource(VehicleService service, VehicleQueryParser queryParser, VehicleDtoMapper mapper) {
        this.service = service;
        this.queryParser = queryParser;
        this.mapper = mapper;
    }

    @Override
    public ResponseEntity<VehiclePageDto> getVehicles(List<String> filter, List<String> sort,
                                                      Integer page, Integer size) {
        var query = queryParser.parse(filter, sort, page, size);
        return ResponseEntity.ok(mapper.toDto(service.find(query)));
    }

    @Override
    public ResponseEntity<VehicleDto> createVehicle(VehicleCreateRequestDto request) {
        Vehicle created = service.create(mapper.toDetails(request));
        return ResponseEntity
                .created(URI.create("vehicles/" + created.getId()))
                .body(mapper.toDto(created));
    }

    @Override
    public ResponseEntity<VehicleDto> getVehicleById(Integer id) {
        return ResponseEntity.ok(mapper.toDto(service.get(id)));
    }

    @Override
    public ResponseEntity<VehicleDto> updateVehicle(Integer id, VehicleUpdateRequestDto request) {
        return ResponseEntity.ok(mapper.toDto(service.replace(id, mapper.toDetails(request))));
    }

    @Override
    public ResponseEntity<Void> deleteVehicle(Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
