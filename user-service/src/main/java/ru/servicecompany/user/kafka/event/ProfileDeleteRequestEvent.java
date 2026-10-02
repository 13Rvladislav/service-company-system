package ru.servicecompany.user.kafka.event;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileDeleteRequestEvent {

    private UUID requestId;

    private UUID authUserId;

    private String role;
}