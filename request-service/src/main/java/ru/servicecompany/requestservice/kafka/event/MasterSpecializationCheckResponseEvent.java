package ru.servicecompany.requestservice.kafka.event;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasterSpecializationCheckResponseEvent {

    private UUID requestId;

    private boolean suitable;

    private String error;
}