package org.itmo.vehicle.infrastructure.web.error;

import org.itmo.vehicle.infrastructure.web.generated.model.ProblemDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.logging.Level;
import java.util.logging.Logger;

@RestControllerAdvice
public class UnexpectedExceptionMapper {

    private static final Logger LOG = Logger.getLogger(UnexpectedExceptionMapper.class.getName());

    @ExceptionHandler(Throwable.class)
    ResponseEntity<ProblemDto> unexpected(Throwable exception) {
        LOG.log(Level.SEVERE, "Unexpected error", exception);
        return Problems.response(500, "Internal service error.");
    }
}
