package org.itmo.vehicle.infrastructure.web.error;

import org.itmo.vehicle.infrastructure.web.InvalidParameterException;
import org.itmo.vehicle.infrastructure.web.generated.model.ProblemDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class InvalidParameterExceptionMapper {

    @ExceptionHandler(InvalidParameterException.class)
    ResponseEntity<ProblemDto> invalid(InvalidParameterException exception) {
        return Problems.response(400, "A query parameter is invalid.",
                List.of(Problems.error(exception.getParameter(), exception.getMessage())));
    }
}
