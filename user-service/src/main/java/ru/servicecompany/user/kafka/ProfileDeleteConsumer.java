package ru.servicecompany.user.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.servicecompany.user.kafka.event.ProfileDeleteRequestEvent;
import ru.servicecompany.user.service.UserProfileService;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProfileDeleteConsumer {

    private final UserProfileService userProfileService;
    private final ProfileDeleteResponseProducer responseProducer;

    @KafkaListener(
            topics = KafkaTopics.USER_PROFILE_DELETE,
            groupId = "user-profile-delete-group",
            containerFactory = "profileDeleteKafkaListenerContainerFactory"
    )
    public void consume(
            ProfileDeleteRequestEvent event
    ) {

        log.info(
                "Kafka <- запрос удаления профиля: requestId={}, authUserId={}, role={}",
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

        if (event.getRole() == null || event.getRole().isBlank()) {
            log.error("role не может быть null или пустым");
            return;
        }

        try {

            userProfileService.deleteProfile(
                    event.getAuthUserId(),
                    event.getRole()
            );

            responseProducer.sendResponse(
                    event.getRequestId(),
                    event.getAuthUserId(),
                    true,
                    null
            );

            log.info(
                    "Профиль успешно удалён: authUserId={}",
                    event.getAuthUserId()
            );

        } catch (Exception e) {

            log.error(
                    "Ошибка удаления профиля: authUserId={}, role={}",
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
                            : "Неизвестная ошибка удаления профиля"
            );
        }
    }
}