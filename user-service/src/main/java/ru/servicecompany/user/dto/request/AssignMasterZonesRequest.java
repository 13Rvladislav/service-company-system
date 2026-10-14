
package ru.servicecompany.user.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class AssignMasterZonesRequest {

    @NotNull(message = "Список зон обязателен")
    private List<UUID> zoneIds;
}
