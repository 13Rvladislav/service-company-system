
package ru.servicecompany.user.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class AssignSpecializationsRequest {

    @NotNull(message = "Список специализаций обязателен")
    private List<UUID> specializationIds;
}
