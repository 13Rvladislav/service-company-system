package ru.servicecompany.user.kafka.event;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileDeleteResponseEvent {

    private UUID requestId;

    private UUID authUserId;

    private boolean success;

    private String reason;
}