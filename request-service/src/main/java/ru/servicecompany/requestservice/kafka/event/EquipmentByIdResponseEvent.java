package ru.servicecompany.requestservice.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.servicecompany.requestservice.dto.response.UserEquipmentResponse;

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