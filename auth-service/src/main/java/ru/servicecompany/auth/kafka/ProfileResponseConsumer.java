package ru.servicecompany.auth.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.servicecompany.auth.kafka.event.ProfileResponseEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProfileResponseConsumer {

    private final ProfileRequestManager profileRequestManager;

    @KafkaListener(
            topics = KafkaTopics.USER_PROFILE_RESPONSE,
            groupId = "auth-profile-response-group",
            containerFactory = "profileResponseKafkaListenerContainerFactory"
    )
    public void consume(ProfileResponseEvent event) {

        log.info("========================================");
        log.info("PROFILE RESPONSE RECEIVED");
        log.info("requestId  = {}", event.getRequestId());
        log.info("authUserId = {}", event.getAuthUserId());
        log.info("role       = {}", event.getRole());
        log.info("========================================");

        if (event.getRequestId() == null) {
            log.error(
                    "Profile response содержит null requestId"
            );
            return;
        }

        profileRequestManager.complete(
                event.getRequestId(),
                event
        );
    }
}