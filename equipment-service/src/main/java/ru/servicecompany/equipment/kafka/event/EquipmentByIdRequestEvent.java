package ru.servicecompany.equipment.kafka.event;

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
public class EquipmentByIdRequestEvent {

    private UUID requestId;

    private UUID authUserId;

    private UUID equipmentId;
}