
package ru.servicecompany.requestservice.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import ru.servicecompany.requestservice.dto.response.ErrorResponse;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(
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
                request,
                Map.of()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
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
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
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
    public ResponseEntity<ErrorResponse> handleUnreadableBody(
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
                request,
                Map.of()
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(
            MissingServletRequestParameterException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Не передан обязательный параметр: " + exception.getParameterName(),
                request,
                Map.of()
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.METHOD_NOT_ALLOWED,
                "HTTP-метод не поддерживается для данного endpoint",
                request,
                Map.of()
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            NoResourceFoundException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                "Запрашиваемый ресурс не найден",
                request,
                Map.of()
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
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
                request,
                Map.of()
        );
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(
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
                request,
                Map.of()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(
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
                request,
                Map.of()
        );
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            Map<String, String> validationErrors
    ) {
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                validationErrors
        );

        return ResponseEntity.status(status).body(body);
    }
}
