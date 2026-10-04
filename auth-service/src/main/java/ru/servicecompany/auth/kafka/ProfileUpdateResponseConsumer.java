package ru.servicecompany.auth.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.servicecompany.auth.kafka.event.ProfileUpdateResponseEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProfileUpdateResponseConsumer {

    private final ProfileUpdateRequestManager requestManager;

    @KafkaListener(
            topics = KafkaTopics.USER_PROFILE_UPDATE_RESPONSE,
            groupId = "auth-profile-update-response-group",
            containerFactory = "profileUpdateResponseKafkaListenerContainerFactory"
    )
    public void consume(
            ProfileUpdateResponseEvent event
    ) {

        log.info(
                "Kafka <- результат обновления профиля: requestId={}, authUserId={}, success={}",
                event.getRequestId(),
                event.getAuthUserId(),
                event.isSuccess()
        );

        if (event.getRequestId() == null) {
            log.error("requestId не может быть null");
            return;
        }

        requestManager.complete(
                event.getRequestId(),
                event
        );
    }
}