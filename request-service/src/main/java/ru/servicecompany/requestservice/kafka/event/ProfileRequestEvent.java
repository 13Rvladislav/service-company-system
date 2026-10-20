package ru.servicecompany.requestservice.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileRequestEvent {

    private UUID requestId;

    /**
     * ID пользователя из auth-service.
     * Используется для обычного запроса профиля.
     */
    private UUID authUserId;

    private String role;

    /**
     * ID профиля мастера из master_profiles.
     * Используется при получении мастера по ID.
     */
    private UUID masterProfileId;
}