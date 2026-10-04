package ru.servicecompany.user.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.servicecompany.user.kafka.event.ProfileUpdateRequestEvent;
import ru.servicecompany.user.service.UserProfileService;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProfileUpdateConsumer {

    private final UserProfileService userProfileService;

    private final ProfileUpdateResponseProducer responseProducer;

    @KafkaListener(
            topics = KafkaTopics.USER_PROFILE_UPDATE,
            groupId = "user-profile-update-group",
            containerFactory = "profileUpdateKafkaListenerContainerFactory"
    )
    public void consume(
            ProfileUpdateRequestEvent event
    ) {

        log.info(
                "Kafka <- запрос обновления профиля: requestId={}, authUserId={}, role={}",
                event.getRequestId(),
                event.getAuthUserId(),
                event.getRole()
        );

        if (event.getRequestId() == null) {
            log.error("requestId не может быть null");
            return;
        }

        if (event.getAuthUserId() == null) {
            log.error("authUserId не может быть null");
            return;
        }

        if (event.getRole() == null
                || event.getRole().isBlank()) {

            log.error("role не может быть null или пустым");
            return;
        }

        try {

            userProfileService.updateProfileByAuthUserId(
                    event
            );

            responseProducer.sendResponse(
                    event.getRequestId(),
                    event.getAuthUserId(),
                    true,
                    null
            );

            log.info(
                    "Профиль успешно обновлён: authUserId={}",
                    event.getAuthUserId()
            );

        } catch (Exception e) {

            log.error(
                    "Ошибка обновления профиля: authUserId={}, role={}",
                    event.getAuthUserId(),
                    event.getRole(),
                    e
            );

            responseProducer.sendResponse(
                    event.getRequestId(),
                    event.getAuthUserId(),
                    false,
                    e.getMessage() != null
                            ? e.getMessage()
                            : "Ошибка обновления профиля"
            );
        }
    }
}