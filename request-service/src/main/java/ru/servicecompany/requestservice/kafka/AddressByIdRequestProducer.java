package ru.servicecompany.requestservice.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.servicecompany.requestservice.kafka.event.AddressByIdRequestEvent;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddressByIdRequestProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void requestAddress(UUID requestId, UUID addressId) {

        AddressByIdRequestEvent event =
                AddressByIdRequestEvent.builder()
                        .requestId(requestId)
                        .addressId(addressId)
                        .build();

        kafkaTemplate.send(
                KafkaTopics.ADDRESS_BY_ID_REQUEST,
                addressId.toString(),
                event
        );
    }
}