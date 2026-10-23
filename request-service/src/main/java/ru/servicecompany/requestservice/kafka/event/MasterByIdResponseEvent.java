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
public class MasterByIdResponseEvent {

    private UUID requestId;

    private UUID masterId;

    private String firstName;

    private String lastName;

    private String middleName;

    private String phone;

    private String error;

    /**
     * Соответствует ли специализация мастера
     * указанному типу оборудования.
     */
    private Boolean suitable;
}