package ru.servicecompany.auth.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.servicecompany.auth.kafka.event.ProfileDeleteResponseEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProfileDeleteResponseConsumer {

    private final ProfileDeleteRequestManager requestManager;

    @KafkaListener(
            topics = "user.profile.delete.response",
            groupId = "auth-profile-delete-response-group",
            containerFactory = "profileDeleteResponseKafkaListenerContainerFactory"
    )
    public void consume(
            ProfileDeleteResponseEvent event
    ) {

        log.info(
                "Kafka <- ответ удаления профиля: requestId={}, authUserId={}, success={}",
                event.getRequestId(),
                event.getAuthUserId(),
                event.isSuccess()
        );

        if (event.getRequestId() == null) {

            log.error(
                    "Ответ удаления профиля содержит null requestId"
            );

            return;
        }

        requestManager.complete(
                event.getRequestId(),
                event
        );
    }
}