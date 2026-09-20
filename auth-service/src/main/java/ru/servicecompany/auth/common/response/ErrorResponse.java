package ru.servicecompany.auth.common.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Единый формат ответа при любой ошибке.
 */
@Data
@Builder
public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

}