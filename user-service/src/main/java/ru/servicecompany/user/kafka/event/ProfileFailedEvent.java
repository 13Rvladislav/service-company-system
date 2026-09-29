package ru.servicecompany.user.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileFailedEvent {

    private UUID authUserId;

    private String reason;
}