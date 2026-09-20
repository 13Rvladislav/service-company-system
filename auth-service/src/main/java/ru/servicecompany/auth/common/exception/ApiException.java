package ru.servicecompany.auth.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Универсальное исключение для всего проекта.
 * Позволяет вернуть любой HTTP статус и сообщение.
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}