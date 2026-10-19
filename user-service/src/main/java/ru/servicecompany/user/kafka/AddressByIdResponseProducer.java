package ru.servicecompany.user.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.servicecompany.user.kafka.event.AddressByIdResponseEvent;

@Service
@RequiredArgsConstructor
public class AddressByIdResponseProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendResponse(AddressByIdResponseEvent event) {

        kafkaTemplate.send(
                KafkaTopics.ADDRESS_BY_ID_RESPONSE,
                event.getAddressId() == null
                        ? event.getRequestId().toString()
                        : event.getAddressId().toString(),
                event
        );
    }
}