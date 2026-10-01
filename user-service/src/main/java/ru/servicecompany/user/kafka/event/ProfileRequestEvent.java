package ru.servicecompany.user.kafka.event;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileRequestEvent {

    /**
     * Идентификатор конкретного запроса.
     *
     * Нужен для сопоставления request -> response.
     */
    private UUID requestId;

    /**
     * ID пользователя из auth-service.
     */
    private UUID authUserId;

    /**
     * Роль пользователя.
     */
    private String role;
}