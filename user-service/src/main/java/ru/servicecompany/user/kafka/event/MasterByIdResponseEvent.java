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
public class MasterByIdResponseEvent {

    private UUID requestId;

    private UUID masterId;

    private String firstName;

    private String lastName;

    private String middleName;

    private String phone;

    /**
     * Результат проверки специализации мастера.
     *
     * true  — мастер подходит для обслуживания оборудования.
     * false — мастер не подходит.
     * null  — проверка специализации не выполнялась.
     */
    private Boolean suitable;

    private String error;
}