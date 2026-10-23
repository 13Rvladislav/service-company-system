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
public class MasterSpecializationCheckRequestEvent {

    private UUID requestId;

    private UUID masterId;

    private UUID equipmentTypeId;
}