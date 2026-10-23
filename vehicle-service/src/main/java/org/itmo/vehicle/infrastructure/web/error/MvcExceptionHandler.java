package org.itmo.vehicle.infrastructure.web.error;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.ConstraintViolation;
import org.itmo.vehicle.infrastructure.web.generated.model.ProblemDto;
import org.itmo.vehicle.infrastructure.web.generated.model.ValidationErrorDto;
import org.springframework.beans.TypeMismatchException;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestControllerAdvice
@Order(0)
class MvcExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOG = Logger.getLogger(MvcExceptionHandler.class.getName());

    private static final String VIOLATES_CONTRACT = "The request violates constraints of the contract.";
    private static final Map<Integer, String> STANDARD_DETAILS = Map.of(
            404, "There is no resource at this address.",
            405, "The resource does not support this method.",
            406, "The resource cannot produce the requested media type.",
            415, "The request body must be application/json.",
            500, "Internal service error.");

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException exception, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<ValidationErrorDto> errors = new ArrayList<>();
        for (ParameterValidationResult result : exception.getParameterValidationResults()) {
            if (result instanceof ParameterErrors body) {
                body.getFieldErrors().forEach(error -> errors.add(fieldError(error)));
                continue;
            }
            String name = nameInRequest(result.getMethodParameter());
            String pointer = result.getContainerIndex() == null ? name : name + "/" + result.getContainerIndex();
            for (MessageSourceResolvable error : result.getResolvableErrors()) {
                ConstraintViolation<?> violation = result.unwrap(error, ConstraintViolation.class);
                errors.add(Problems.error(pointer, ConstraintViolationExceptionMapper.message(violation)));
            }
        }
        return badRequest(VIOLATES_CONTRACT, errors, headers);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<ValidationErrorDto> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(MvcExceptionHandler::fieldError)
                .toList();
        return badRequest(VIOLATES_CONTRACT, errors, headers);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException exception, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String name = exception.getPropertyName();
        return badRequest("Parameter '" + name + "' has an invalid value.",
                List.of(Problems.error(name, "is not a valid value of this parameter")), headers);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        for (Throwable cause = exception.getCause(); cause != null; cause = cause.getCause()) {
            if (cause instanceof JsonProcessingException json) {
                return withHeaders(JsonProcessingExceptionMapper.problem(json), headers);
            }
        }
        return badRequest(VIOLATES_CONTRACT, List.of(Problems.error("", "must be present")), headers);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception exception, Object body, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        if (status.is5xxServerError()) {
            LOG.log(Level.SEVERE, "Error while serving " + request.getDescription(false), exception);
        }
        String detail = STANDARD_DETAILS.getOrDefault(status.value(), exception.getMessage());
        return withHeaders(Problems.response(status.value(), detail), headers);
    }

    private static ResponseEntity<Object> badRequest(String detail, List<ValidationErrorDto> errors, HttpHeaders headers) {
        List<ValidationErrorDto> sorted = errors.stream().sorted(Comparator.comparing(ValidationErrorDto::getField)).toList();
        return withHeaders(Problems.response(400, detail, sorted), headers);
    }

    private static ResponseEntity<Object> withHeaders(ResponseEntity<ProblemDto> problem, HttpHeaders headers) {
        HttpHeaders merged = new HttpHeaders();
        if (headers != null) {
            merged.putAll(headers);
        }
        merged.putAll(problem.getHeaders());
        return new ResponseEntity<>(problem.getBody(), merged, problem.getStatusCode());
    }

    private static ValidationErrorDto fieldError(FieldError error) {
        String pointer = error.getField().replace("[", ".").replace("]", "").replace('.', '/');
        ConstraintViolation<?> violation = error.contains(ConstraintViolation.class)
                ? error.unwrap(ConstraintViolation.class) : null;
        String message = violation != null ? ConstraintViolationExceptionMapper.message(violation) : error.getDefaultMessage();
        return Problems.error(pointer, message);
    }

    private static String nameInRequest(MethodParameter parameter) {
        PathVariable path = parameter.getParameterAnnotation(PathVariable.class);
        if (path != null && !path.value().isEmpty()) {
            return path.value();
        }
        RequestParam query = parameter.getParameterAnnotation(RequestParam.class);
        if (query != null && !query.value().isEmpty()) {
            return query.value();
        }
        return parameter.getParameterName();
    }
}
