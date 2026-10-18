package ru.servicecompany.requestservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.servicecompany.requestservice.kafka.event.ProfileRequestEvent;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileRequestProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void requestProfile(
            UUID requestId,
            UUID authUserId,
            String role
    ) {
        ProfileRequestEvent event = ProfileRequestEvent.builder()
                .requestId(requestId)
                .authUserId(authUserId)
                .role(role)
                .build();

        kafkaTemplate.send(
                KafkaTopics.USER_PROFILE_REQUEST,
                authUserId.toString(),
                event
        );
        log.info(
                "REQUEST SERVICE: отправлен запрос профиля, requestId={}, authUserId={}",
                requestId,
                authUserId
        );
    }
}