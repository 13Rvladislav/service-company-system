
package ru.servicecompany.auth.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecializationResponse {

    private UUID id;

    private String name;

    private Set<UUID> equipmentTypeIds;
}
