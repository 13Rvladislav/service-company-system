package ru.servicecompany.auth.kafka.event;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileRequestEvent {

    /**
     * Уникальный идентификатор запроса.
     *
     * Нужен для того, чтобы auth-service
     * смог сопоставить response с конкретным HTTP-запросом.
     */
    private UUID requestId;

    /**
     * ID пользователя из auth-service.
     */
    private UUID authUserId;

    /**
     * Роль пользователя.
     *
     * CLIENT
     * ENGINEER
     * DISPATCHER
     * ADMIN
     */
    private String role;
}