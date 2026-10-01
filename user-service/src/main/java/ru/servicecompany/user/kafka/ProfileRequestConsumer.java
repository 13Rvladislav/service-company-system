package ru.servicecompany.user.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.servicecompany.user.dto.response.UserProfileResponse;
import ru.servicecompany.user.kafka.event.ProfileRequestEvent;
import ru.servicecompany.user.service.UserProfileService;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProfileRequestConsumer {

    private final UserProfileService userProfileService;
    private final ProfileResponseProducer profileResponseProducer;

    /**
     * Получает запрос от auth-service
     * на получение профильных данных пользователя.
     */
    @KafkaListener(
            topics = KafkaTopics.USER_PROFILE_REQUEST,
            groupId = "user-profile-request-group",
            containerFactory = "profileRequestKafkaListenerContainerFactory"
    )
    public void consume(ProfileRequestEvent event) {

        log.info("========================================");
        log.info("PROFILE REQUEST RECEIVED");
        log.info("requestId  = {}", event.getRequestId());
        log.info("authUserId = {}", event.getAuthUserId());
        log.info("role       = {}", event.getRole());
        log.info("========================================");

        if (event.getRequestId() == null) {
            throw new IllegalArgumentException(
                    "requestId не может быть null"
            );
        }

        if (event.getAuthUserId() == null) {
            throw new IllegalArgumentException(
                    "authUserId не может быть null"
            );
        }

        if (event.getRole() == null || event.getRole().isBlank()) {
            throw new IllegalArgumentException(
                    "role не может быть null или пустым"
            );
        }

        try {

            UserProfileResponse profile =
                    userProfileService.getProfileByAuthUserId(
                            event.getAuthUserId(),
                            event.getRole()
                    );

            profileResponseProducer.profileResponse(
                    event.getRequestId(),
                    event.getAuthUserId(),
                    profile
            );

            log.info("========================================");
            log.info("PROFILE RESPONSE SENT");
            log.info("requestId  = {}", event.getRequestId());
            log.info("authUserId = {}", event.getAuthUserId());
            log.info("role       = {}", event.getRole());
            log.info("========================================");

        } catch (Exception e) {

            log.error(
                    "Ошибка получения профиля: requestId={}, authUserId={}, role={}",
                    event.getRequestId(),
                    event.getAuthUserId(),
                    event.getRole(),
                    e
            );

            /*
             * Пока намеренно пробрасываем exception.
             *
             * Для операции просмотра карточки
             * auth-service должен получить ошибку,
             * а не получить пустую карточку.
             */
            throw e;
        }
    }
}