package org.itmo.vehicle.infrastructure.web.error;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.itmo.vehicle.infrastructure.web.generated.model.ProblemDto;
import org.itmo.vehicle.infrastructure.web.generated.model.ValidationErrorDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class ConstraintViolationExceptionMapper {

    private static final Map<String, String> PATTERN_MEANINGS = Map.of(
            "^\\s*\\S[\\s\\S]*$",
            "must contain a non-whitespace character",
            "^(id|name|coordinates\\.x|coordinates\\.y|creationDate|enginePower|numberOfWheels|mileage|type|fuelType)"
                    + ":(eq|neq|gt|gte|lt|lte|like|in|isnull):.+$",
            "must have the form field:operator:value with a known field and operator",
            "^-?(id|name|coordinates\\.x|coordinates\\.y|creationDate|enginePower|numberOfWheels|mileage|type|fuelType)$",
            "must be a field name, optionally prefixed with - for descending order");

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ProblemDto> invalid(ConstraintViolationException exception) {
        List<ValidationErrorDto> errors = exception.getConstraintViolations().stream()
                .map(violation -> Problems.error(pointer(violation), message(violation)))
                .sorted(Comparator.comparing(ValidationErrorDto::getField))
                .toList();
        return Problems.response(400, "The request violates constraints of the contract.", errors);
    }

    private static String message(ConstraintViolation<?> violation) {
        if (violation.getConstraintDescriptor().getAnnotation() instanceof Pattern pattern) {
            return PATTERN_MEANINGS.getOrDefault(pattern.regexp(), violation.getMessage());
        }
        return violation.getMessage();
    }

    static String pointer(ConstraintViolation<?> violation) {
        List<Path.Node> nodes = new ArrayList<>();
        violation.getPropertyPath().forEach(nodes::add);
        List<String> parts = new ArrayList<>();
        for (int i = 0; i < nodes.size(); i++) {
            Path.Node node = nodes.get(i);
            boolean leaf = i == nodes.size() - 1;
            switch (node.getKind()) {
                case PARAMETER -> {
                    boolean bodyProperty = !leaf && nodes.get(i + 1).getKind() == jakarta.validation.ElementKind.PROPERTY;
                    boolean missingBody = leaf && isNotNull(violation);
                    if (!bodyProperty && !missingBody) {
                        parts.add(node.getName());
                    }
                }
                case PROPERTY -> {
                    if (node.getIndex() != null) {
                        parts.add(String.valueOf(node.getIndex()));
                    }
                    parts.add(node.getName());
                }
                case CONTAINER_ELEMENT -> {
                    if (node.getIndex() != null) {
                        parts.add(String.valueOf(node.getIndex()));
                    }
                }
                default -> {
                }
            }
        }
        return String.join("/", parts);
    }

    private static boolean isNotNull(ConstraintViolation<?> violation) {
        return violation.getConstraintDescriptor().getAnnotation() instanceof NotNull;
    }
}
