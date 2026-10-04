package ru.servicecompany.auth.kafka.event;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
public class ProfileUpdateResponseEvent {

    private UUID requestId;

    private UUID authUserId;

    private boolean success;

    private String reason;

    public ProfileUpdateResponseEvent(
            UUID requestId,
            UUID authUserId,
            boolean success,
            String reason
    ) {
        this.requestId = requestId;
        this.authUserId = authUserId;
        this.success = success;
        this.reason = reason;
    }
}