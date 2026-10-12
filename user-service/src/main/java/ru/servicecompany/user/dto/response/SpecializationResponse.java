
package ru.servicecompany.user.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.Set;
import java.util.UUID;

@Getter
@Builder
public class SpecializationResponse {

    private UUID id;
    private String name;
    private Set<UUID> equipmentTypeIds;
}
