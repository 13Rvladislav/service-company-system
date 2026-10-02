package ru.servicecompany.auth.kafka.event;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileDeleteRequestEvent {

    /**
     * Уникальный ID операции удаления.
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