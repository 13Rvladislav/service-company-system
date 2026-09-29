package ru.servicecompany.auth.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.servicecompany.auth.kafka.event.ProfileFailedEvent;
import ru.servicecompany.auth.repository.UserRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProfileResultConsumer {

    private static final String PROFILE_FAILED_TOPIC = "user.profile.failed";

    private final UserRepository userRepository;

    @Transactional
    @KafkaListener(
            topics = PROFILE_FAILED_TOPIC,
            groupId = "auth-service-group",
            containerFactory = "profileFailedKafkaListenerContainerFactory"
    )
    public void profileFailed(ProfileFailedEvent event) {

        log.error(
                "PROFILE CREATION FAILED RECEIVED: authUserId={}, reason={}",
                event.getAuthUserId(),
                event.getReason()
        );

        if (event.getAuthUserId() == null) {
            log.error("PROFILE_FAILED содержит null authUserId");
            return;
        }

        if (!userRepository.existsById(event.getAuthUserId())) {
            log.warn(
                    "Auth User уже отсутствует: {}",
                    event.getAuthUserId()
            );
            return;
        }

        userRepository.deleteById(event.getAuthUserId());

        log.error(
                "Auth User удалён после ошибки создания профиля: {}",
                event.getAuthUserId()
        );
    }
}