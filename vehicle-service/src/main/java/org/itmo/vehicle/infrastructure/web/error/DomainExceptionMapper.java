package org.itmo.vehicle.infrastructure.web.error;

import org.itmo.vehicle.domain.DomainException;
import org.itmo.vehicle.domain.InvariantViolationException;
import org.itmo.vehicle.domain.NoVehiclesException;
import org.itmo.vehicle.domain.VehicleNotFoundException;
import org.itmo.vehicle.infrastructure.web.generated.model.ProblemDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class DomainExceptionMapper {

    @ExceptionHandler(InvariantViolationException.class)
    ResponseEntity<ProblemDto> invariant(InvariantViolationException violation) {
        return Problems.response(400, "The vehicle violates an integrity constraint.",
                List.of(Problems.error(Problems.pointer(violation.getField()), violation.getMessage())));
    }

    @ExceptionHandler({VehicleNotFoundException.class, NoVehiclesException.class})
    ResponseEntity<ProblemDto> notFound(DomainException exception) {
        return Problems.response(404, exception.getMessage());
    }
}
