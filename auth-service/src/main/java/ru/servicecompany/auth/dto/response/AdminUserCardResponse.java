
package ru.servicecompany.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserCardResponse {

    private UUID id;
    private UUID authUserId;

    private String firstName;
    private String lastName;
    private String middleName;

    private String email;
    private String phone;
    private String role;

    // Данные клиента
    private String city;
    private String street;
    private String house;
    private String apartment;

    // Данные мастера
    private String employeeNumber;
    private String specialization;
    private List<SpecializationResponse> specializations;
    private String status;
    private List<ZoneResponse> zones;

    // Данные диспетчера и администратора
    private String department;
    private String position;

    // Дополнительные данные карточки
    private Boolean enabled;
    private Boolean hasAvatar;
    private UUID cityId;
    private UUID streetId;
    private UUID houseId;
}
