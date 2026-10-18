package ru.servicecompany.equipment.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.servicecompany.equipment.dto.response.UserEquipmentResponse;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentResponseEvent {

    private UUID requestId;

    private UUID authUserId;

    private List<UserEquipmentResponse> equipment;

    private String error;
}