package ru.servicecompany.auth.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.servicecompany.auth.common.exception.ApiException;
import ru.servicecompany.auth.dto.request.CreateEmployeeRequest;
import ru.servicecompany.auth.entity.User;
import ru.servicecompany.auth.kafka.event.UserProfileCreateEvent;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileProducer {

    private static final String TOPIC = "user.profile.create";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendClientProfileCreate(User user) {

        UserProfileCreateEvent event = new UserProfileCreateEvent();

        event.setAuthUserId(user.getId());
        event.setFirstName(user.getFirstName());
        event.setLastName(user.getLastName());
        event.setMiddleName(user.getMiddleName());
        event.setPhone(user.getPhone());
        event.setRole("CLIENT");

        kafkaTemplate.send(
                TOPIC,
                user.getId().toString(),
                event
        );

        log.info(
                "Kafka -> создание профиля CLIENT: authUserId={}",
                user.getId()
        );
    }

    public void sendEmployeeProfileCreate(
            User user,
            CreateEmployeeRequest request
    ) {

        UserProfileCreateEvent event = new UserProfileCreateEvent();

        event.setAuthUserId(user.getId());
        event.setFirstName(user.getFirstName());
        event.setLastName(user.getLastName());
        event.setMiddleName(user.getMiddleName());
        event.setPhone(user.getPhone());

        event.setEmployeeNumber(request.getEmployeeNumber());
        event.setDepartment(request.getDepartment());
        event.setPosition(request.getPosition());
        event.setRole(request.getRole().name());

        // Передаём список UUID специализаций.
        event.setSpecializationIds(request.getSpecializationIds());

        // Старое поле временно сохраняем для совместимости.
        event.setSpecialization(request.getSpecialization());

        if (request.getZoneId() != null
                && !request.getZoneId().isBlank()) {

            try {
                event.setZoneId(UUID.fromString(request.getZoneId()));
            } catch (IllegalArgumentException e) {
                throw new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "Некорректный UUID зоны"
                );
            }
        }

        kafkaTemplate.send(
                TOPIC,
                user.getId().toString(),
                event
        );

        log.info(
                "Kafka -> создание профиля сотрудника: authUserId={}, role={}",
                user.getId(),
                request.getRole()
        );
    }
}