
package ru.servicecompany.requestservice.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

import ru.servicecompany.requestservice.kafka.event.*;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    private Map<String, Object> createConsumerProperties(
            Environment environment,
            String groupId
    ) {
        Map<String, Object> properties = new HashMap<>();

        properties.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                environment.getRequiredProperty(
                        "spring.kafka.bootstrap-servers"
                )
        );

        properties.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                groupId
        );

        properties.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "latest"
        );

        properties.put(
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                true
        );

        return properties;
    }

    // ---------------------------------------------------------
    // Consumer ответа User Service с профилем клиента
    // ---------------------------------------------------------

    @Bean
    public ConsumerFactory<String, ProfileResponseEvent>
    profileResponseConsumerFactory(Environment environment) {

        JacksonJsonDeserializer<ProfileResponseEvent> deserializer =
                new JacksonJsonDeserializer<>(
                        ProfileResponseEvent.class
                );

        deserializer.ignoreTypeHeaders();

        return new DefaultKafkaConsumerFactory<>(
                createConsumerProperties(
                        environment,
                        "request-service-profile-response-group"
                ),
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ProfileResponseEvent>
    profileResponseKafkaListenerContainerFactory(
            ConsumerFactory<String, ProfileResponseEvent>
                    profileResponseConsumerFactory
    ) {

        ConcurrentKafkaListenerContainerFactory<String, ProfileResponseEvent>
                factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(
                profileResponseConsumerFactory
        );

        factory.setConcurrency(1);

        return factory;
    }

    // ---------------------------------------------------------
    // Consumer ответа Equipment Service со списком оборудования
    // ---------------------------------------------------------

    @Bean
    public ConsumerFactory<String, EquipmentResponseEvent>
    equipmentResponseConsumerFactory(Environment environment) {

        JacksonJsonDeserializer<EquipmentResponseEvent> deserializer =
                new JacksonJsonDeserializer<>(
                        EquipmentResponseEvent.class
                );

        deserializer.ignoreTypeHeaders();

        return new DefaultKafkaConsumerFactory<>(
                createConsumerProperties(
                        environment,
                        "request-service-equipment-response-group"
                ),
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EquipmentResponseEvent>
    equipmentResponseKafkaListenerContainerFactory(
            ConsumerFactory<String, EquipmentResponseEvent>
                    equipmentResponseConsumerFactory
    ) {

        ConcurrentKafkaListenerContainerFactory<String, EquipmentResponseEvent>
                factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(
                equipmentResponseConsumerFactory
        );

        factory.setConcurrency(1);

        return factory;
    }

    // ---------------------------------------------------------
    // Новый consumer ответа Equipment Service по ID
    // ---------------------------------------------------------

    @Bean
    public ConsumerFactory<String, EquipmentByIdResponseEvent>
    equipmentByIdResponseConsumerFactory(Environment environment) {

        JacksonJsonDeserializer<EquipmentByIdResponseEvent> deserializer =
                new JacksonJsonDeserializer<>(
                        EquipmentByIdResponseEvent.class
                );

        deserializer.ignoreTypeHeaders();

        return new DefaultKafkaConsumerFactory<>(
                createConsumerProperties(
                        environment,
                        "request-service-equipment-by-id-group"
                ),
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EquipmentByIdResponseEvent>
    equipmentByIdResponseKafkaListenerContainerFactory(
            ConsumerFactory<String, EquipmentByIdResponseEvent>
                    equipmentByIdResponseConsumerFactory
    ) {

        ConcurrentKafkaListenerContainerFactory<String, EquipmentByIdResponseEvent>
                factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(
                equipmentByIdResponseConsumerFactory
        );

        factory.setConcurrency(1);

        return factory;
    }

    // ---------------------------------------------------------
    // Стандартная фабрика listener
    // ---------------------------------------------------------

    @Bean(name = "kafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, ProfileResponseEvent>
    kafkaListenerContainerFactory(
            ConsumerFactory<String, ProfileResponseEvent>
                    profileResponseConsumerFactory
    ) {

        ConcurrentKafkaListenerContainerFactory<String, ProfileResponseEvent>
                factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(
                profileResponseConsumerFactory
        );

        factory.setConcurrency(1);

        return factory;
    }
    @Bean
    public ConsumerFactory<String, AddressByIdResponseEvent>
    addressByIdResponseConsumerFactory(Environment environment) {

        JacksonJsonDeserializer<AddressByIdResponseEvent> deserializer =
                new JacksonJsonDeserializer<>(
                        AddressByIdResponseEvent.class
                );

        deserializer.ignoreTypeHeaders();

        return new DefaultKafkaConsumerFactory<>(
                createConsumerProperties(
                        environment,
                        "request-service-address-by-id-group"
                ),
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<
            String,
            AddressByIdResponseEvent
            >
    addressByIdResponseKafkaListenerContainerFactory(
            ConsumerFactory<String, AddressByIdResponseEvent>
                    addressByIdResponseConsumerFactory
    ) {

        ConcurrentKafkaListenerContainerFactory<
                String,
                AddressByIdResponseEvent
                > factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(
                addressByIdResponseConsumerFactory
        );

        factory.setConcurrency(1);

        return factory;
    }

    // ---------------------------------------------------------
// Consumer ответа User Service с данными мастера
// ---------------------------------------------------------

    @Bean
    public ConsumerFactory<String, MasterByIdResponseEvent>
    masterByIdResponseConsumerFactory(Environment environment) {

        JacksonJsonDeserializer<MasterByIdResponseEvent> deserializer =
                new JacksonJsonDeserializer<>(
                        MasterByIdResponseEvent.class
                );

        deserializer.ignoreTypeHeaders();

        return new DefaultKafkaConsumerFactory<>(
                createConsumerProperties(
                        environment,
                        "request-service-master-by-id-group"
                ),
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MasterByIdResponseEvent>
    masterByIdResponseKafkaListenerContainerFactory(
            ConsumerFactory<String, MasterByIdResponseEvent>
                    masterByIdResponseConsumerFactory
    ) {

        ConcurrentKafkaListenerContainerFactory<String, MasterByIdResponseEvent>
                factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(
                masterByIdResponseConsumerFactory
        );

        factory.setConcurrency(1);

        return factory;
    }
}
