package org.itmo.vehicle.infrastructure.web.error;

import org.itmo.vehicle.infrastructure.web.generated.model.ProblemDto;
import org.itmo.vehicle.infrastructure.web.generated.model.ValidationErrorDto;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

final class Problems {

    static final MediaType PROBLEM_JSON = new MediaType("application", "problem+json");

    private Problems() {
    }

    static ResponseEntity<ProblemDto> response(int status, String detail, List<ValidationErrorDto> errors) {
        ProblemDto problem = new ProblemDto()
                .title(reason(status))
                .status(status)
                .detail(detail)
                .instance(instance())
                .errors(errors);
        return ResponseEntity.status(status).contentType(PROBLEM_JSON).body(problem);
    }

    static ResponseEntity<ProblemDto> response(int status, String detail) {
        return response(status, detail, List.of());
    }

    static ValidationErrorDto error(String field, String message) {
        return new ValidationErrorDto().field(field).message(message);
    }

    static String pointer(String propertyPath) {
        return propertyPath.replace('.', '/');
    }

    private static String reason(int status) {
        return switch (status) {
            case 400 -> "Bad Request";
            case 404 -> "Not Found";
            case 405 -> "Method Not Allowed";
            case 406 -> "Not Acceptable";
            case 415 -> "Unsupported Media Type";
            case 500 -> "Internal Server Error";
            default -> "HTTP " + status;
        };
    }

    private static String instance() {
        var attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs == null ? null : attrs.getRequest().getRequestURI();
    }
}
