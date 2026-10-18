package ru.servicecompany.equipment.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

import ru.servicecompany.equipment.kafka.event.EquipmentRequestEvent;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, EquipmentRequestEvent>
    equipmentRequestConsumerFactory(Environment environment) {

        Map<String, Object> properties = new HashMap<>();

        properties.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                environment.getRequiredProperty(
                        "spring.kafka.bootstrap-servers"
                )
        );

        properties.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "equipment-service-user-equipment-group"
        );

        properties.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "latest"
        );

        properties.put(
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                true
        );

        JacksonJsonDeserializer<EquipmentRequestEvent> deserializer =
                new JacksonJsonDeserializer<>(
                        EquipmentRequestEvent.class
                );

        deserializer.ignoreTypeHeaders();

        return new DefaultKafkaConsumerFactory<>(
                properties,
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EquipmentRequestEvent>
    equipmentRequestKafkaListenerContainerFactory(
            ConsumerFactory<String, EquipmentRequestEvent>
                    equipmentRequestConsumerFactory
    ) {

        ConcurrentKafkaListenerContainerFactory<String, EquipmentRequestEvent>
                factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(
                equipmentRequestConsumerFactory
        );

        factory.setConcurrency(1);

        return factory;
    }
}