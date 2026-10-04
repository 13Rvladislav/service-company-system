package ru.servicecompany.auth.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.servicecompany.auth.kafka.event.ProfileUpdateRequestEvent;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileUpdateRequestProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void updateProfile(
            UUID requestId,
            UUID authUserId,
            String role,
            String firstName,
            String lastName,
            String middleName,
            String phone,
            UUID houseId,
            String apartment,
            String employeeNumber,
            String specialization,
            UUID zoneId,
            String status,
            String department,
            String position
    ) {

        ProfileUpdateRequestEvent event =
                ProfileUpdateRequestEvent.builder()
                        .requestId(requestId)
                        .authUserId(authUserId)
                        .role(role)
                        .firstName(firstName)
                        .lastName(lastName)
                        .middleName(middleName)
                        .phone(phone)
                        .houseId(houseId)
                        .apartment(apartment)
                        .employeeNumber(employeeNumber)
                        .specialization(specialization)
                        .zoneId(zoneId)
                        .status(status)
                        .department(department)
                        .position(position)
                        .build();

        kafkaTemplate.send(
                KafkaTopics.USER_PROFILE_UPDATE,
                authUserId.toString(),
                event
        );

        log.info(
                "Kafka -> запрос обновления профиля: requestId={}, authUserId={}, role={}",
                requestId,
                authUserId,
                role
        );
    }
}