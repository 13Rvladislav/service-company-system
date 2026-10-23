package ru.servicecompany.user.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasterByIdRequestEvent {

    private UUID requestId;

    private UUID masterId;

    /**
     * Тип оборудования, для которого проверяется специализация мастера.
     * Если null — проверка специализации не выполняется.
     */
    private UUID equipmentTypeId;
}