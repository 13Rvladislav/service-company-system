package ru.servicecompany.user.kafka.event;

import lombok.*;
import ru.servicecompany.user.dto.response.SpecializationResponse;
import ru.servicecompany.user.dto.response.ZoneResponse;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileResponseEvent {

    // Корреляция Kafka
    private UUID requestId;

    // Пользователь и профиль
    private UUID authUserId;
    private UUID id;
    private String role;

    // Основные данные
    private String firstName;
    private String lastName;
    private String middleName;
    private String phone;
    private Boolean hasAvatar;

    // CLIENT: адрес
    private UUID cityId;
    private UUID streetId;
    private UUID houseId;
    private UUID zoneId;

    private String cityName;
    private String streetName;
    private String houseNumber;

    private String apartment;

    // ENGINEER
    private List<ZoneResponse> zones;
    private String employeeNumber;
    private List<SpecializationResponse> specializations;
    private String status;

    // DISPATCHER
    private String department;

    // ADMIN
    private String position;
}