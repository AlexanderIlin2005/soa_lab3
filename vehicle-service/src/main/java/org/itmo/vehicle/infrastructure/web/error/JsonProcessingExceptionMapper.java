package org.itmo.vehicle.infrastructure.web.error;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import org.itmo.vehicle.infrastructure.web.generated.model.ProblemDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class JsonProcessingExceptionMapper {

    @ExceptionHandler(JsonProcessingException.class)
    ResponseEntity<ProblemDto> badJson(JsonProcessingException exception) {
        if (exception instanceof JsonMappingException mapping && !mapping.getPath().isEmpty()) {
            return Problems.response(400, "The request body does not match the schema.",
                    List.of(Problems.error(pointer(mapping), describe(mapping))));
        }
        return Problems.response(400, "The request body is not valid JSON: " + exception.getOriginalMessage());
    }

    private static String pointer(JsonMappingException exception) {
        return exception.getPath().stream()
                .map(reference -> reference.getFieldName() != null
                        ? reference.getFieldName()
                        : String.valueOf(reference.getIndex()))
                .collect(Collectors.joining("/"));
    }

    private static String describe(JsonMappingException exception) {
        if (exception instanceof UnrecognizedPropertyException) {
            return "is not a property of this object";
        }
        if (exception instanceof ValueInstantiationException && exception.getCause() != null) {
            return exception.getCause().getMessage();
        }
        if (exception instanceof InvalidFormatException format) {
            return "has an invalid value: " + format.getValue();
        }
        if (exception instanceof MismatchedInputException) {
            return "has a value of the wrong type";
        }
        return "has an invalid value";
    }
}
