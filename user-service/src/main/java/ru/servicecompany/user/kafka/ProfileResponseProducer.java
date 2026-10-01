package ru.servicecompany.user.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.servicecompany.user.dto.response.UserProfileResponse;
import ru.servicecompany.user.kafka.event.ProfileResponseEvent;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileResponseProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Отправляет профиль обратно в auth-service.
     */
    public void profileResponse(
            UUID requestId,
            UUID authUserId,
            UserProfileResponse profile
    ) {

        ProfileResponseEvent event =
                ProfileResponseEvent.builder()
                        .requestId(requestId)
                        .authUserId(authUserId)

                        .id(profile.getId())

                        .role(profile.getRole())

                        .firstName(profile.getFirstName())
                        .lastName(profile.getLastName())
                        .middleName(profile.getMiddleName())
                        .phone(profile.getPhone())

                        .hasAvatar(profile.getHasAvatar())

                        .cityId(profile.getCityId())
                        .streetId(profile.getStreetId())
                        .houseId(profile.getHouseId())
                        .apartment(profile.getApartment())

                        .employeeNumber(profile.getEmployeeNumber())
                        .specialization(profile.getSpecialization())
                        .status(profile.getStatus())

                        .department(profile.getDepartment())
                        .position(profile.getPosition())

                        .build();

        kafkaTemplate.send(
                KafkaTopics.USER_PROFILE_RESPONSE,
                authUserId.toString(),
                event
        );

        log.info(
                "Kafka -> профиль отправлен обратно: requestId={}, authUserId={}, role={}",
                requestId,
                authUserId,
                profile.getRole()
        );
    }
}