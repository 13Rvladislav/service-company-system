
package ru.servicecompany.auth.common.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import ru.servicecompany.auth.common.exception.ApiException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApiException(
            ApiException exception,
            HttpServletRequest request
    ) {
        log.warn(
                "API error: method={}, path={}, status={}, message={}",
                request.getMethod(),
                request.getRequestURI(),
                exception.getStatus().value(),
                exception.getMessage()
        );

        return buildResponse(
                exception.getStatus(),
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> errors = new LinkedHashMap<>();

        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(
                        error.getField(),
                        error.getDefaultMessage() != null
                                ? error.getDefaultMessage()
                                : "Некорректное значение"
                )
        );

        log.warn(
                "Validation error: method={}, path={}, errors={}",
                request.getMethod(),
                request.getRequestURI(),
                errors
        );

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Ошибка валидации входных данных",
                request,
                errors
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        Map<String, String> errors = new LinkedHashMap<>();

        exception.getConstraintViolations().forEach(violation ->
                errors.putIfAbsent(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()
                )
        );

        log.warn(
                "Constraint violation: method={}, path={}, errors={}",
                request.getMethod(),
                request.getRequestURI(),
                errors
        );

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Нарушены ограничения входных данных",
                request,
                errors
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadableBody(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        log.warn(
                "Invalid request body: method={}, path={}",
                request.getMethod(),
                request.getRequestURI()
        );

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Некорректный формат тела запроса",
                request
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParameter(
            MissingServletRequestParameterException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Не передан обязательный параметр: " + exception.getParameterName(),
                request
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.METHOD_NOT_ALLOWED,
                "HTTP-метод не поддерживается для данного endpoint",
                request
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(
            NoResourceFoundException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                "Запрашиваемый ресурс не найден",
                request
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        log.error(
                "Database constraint violation: method={}, path={}",
                request.getMethod(),
                request.getRequestURI(),
                exception
        );

        return buildResponse(
                HttpStatus.CONFLICT,
                "Операция нарушает ограничения базы данных",
                request
        );
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(
            ResponseStatusException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());

        log.warn(
                "HTTP error: method={}, path={}, status={}, message={}",
                request.getMethod(),
                request.getRequestURI(),
                status.value(),
                exception.getReason()
        );

        return buildResponse(
                status,
                exception.getReason() != null
                        ? exception.getReason()
                        : status.getReasonPhrase(),
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request
    ) {
        log.error(
                "Unexpected error: method={}, path={}, query={}",
                request.getMethod(),
                request.getRequestURI(),
                request.getQueryString(),
                exception
        );

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Внутренняя ошибка сервера",
                request
        );
    }

    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request
    ) {
        return buildResponse(status, message, request, Map.of());
    }

    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            Map<String, String> validationErrors
    ) {
        Map<String, Object> body = new LinkedHashMap<>();

        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", request.getRequestURI());
        body.put("correlationId", MDC.get("correlationId"));

        if (!validationErrors.isEmpty()) {
            body.put("validationErrors", validationErrors);
        }

        return ResponseEntity.status(status).body(body);
    }
}
