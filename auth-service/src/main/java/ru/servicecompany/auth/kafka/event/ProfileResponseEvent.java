
        package ru.servicecompany.auth.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.servicecompany.auth.dto.response.SpecializationResponse;
import ru.servicecompany.auth.dto.response.ZoneResponse;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileResponseEvent {

    private UUID requestId;
    private UUID authUserId;
    private UUID id;

    private String role;

    private String firstName;
    private String lastName;
    private String middleName;
    private String phone;

    private Boolean hasAvatar;

    // CLIENT
    private UUID cityId;
    private UUID streetId;
    private UUID houseId;
    private String apartment;

    // ENGINEER
    private String employeeNumber;
    private List<SpecializationResponse> specializations;
    private String status;
    private List<ZoneResponse> zones;

    // DISPATCHER
    private String department;

    // ADMIN
    private String position;
}
