package ru.servicecompany.auth.kafka.event;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileDeleteResponseEvent {

    /**
     * ID операции удаления.
     */
    private UUID requestId;

    /**
     * ID пользователя.
     */
    private UUID authUserId;

    /**
     * Удалён ли профиль успешно.
     */
    private boolean success;

    /**
     * Причина ошибки, если удаление не удалось.
     */
    private String reason;
}