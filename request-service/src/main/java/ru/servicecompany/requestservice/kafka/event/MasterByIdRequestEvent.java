package ru.servicecompany.requestservice.kafka.event;

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
public class MasterByIdRequestEvent {

    private UUID requestId;

    private UUID masterId;

    /**
     * Если задан — проверяем, есть ли у мастера
     * специализация для указанного типа оборудования.
     *
     * Если null — обычное получение мастера по ID.
     */
    private UUID equipmentTypeId;
}