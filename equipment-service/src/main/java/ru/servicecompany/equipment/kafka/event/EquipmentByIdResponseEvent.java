package ru.servicecompany.equipment.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.servicecompany.equipment.dto.response.UserEquipmentResponse;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentByIdResponseEvent {

    private UUID requestId;

    private UUID authUserId;

    private UUID equipmentId;

    private UserEquipmentResponse equipment;

    private String error;
}