package ru.servicecompany.user.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import ru.servicecompany.user.kafka.event.ProfileRequestEvent;
import ru.servicecompany.user.kafka.event.UserProfileCreateEvent;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    private static final String BOOTSTRAP_SERVERS =
            "localhost:29092";

    private static final String GROUP_ID =
            "user-service-group";

    /**
     * ============================================================
     * EXISTING USER PROFILE CREATE CONSUMER
     * ============================================================
     */

    @Bean
    public ConsumerFactory<String, UserProfileCreateEvent>
    consumerFactory() {

        Map<String, Object> props = new HashMap<>();

        props.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                BOOTSTRAP_SERVERS
        );

        props.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                GROUP_ID
        );

        props.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

        props.put(
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                true
        );

        props.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class
        );

        JacksonJsonDeserializer<UserProfileCreateEvent>
                deserializer =
                new JacksonJsonDeserializer<>(
                        UserProfileCreateEvent.class
                );

        deserializer.ignoreTypeHeaders();

        deserializer.trustedPackages(
                "ru.servicecompany.user.kafka.event"
        );

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean(name = "kafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<
            String,
            UserProfileCreateEvent
            >
    kafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<
                String,
                UserProfileCreateEvent
                > factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(
                consumerFactory()
        );

        return factory;
    }

    /**
     * ============================================================
     * PROFILE REQUEST CONSUMER
     * ============================================================
     */

    @Bean
    public ConsumerFactory<String, ProfileRequestEvent>
    profileRequestConsumerFactory() {

        Map<String, Object> props = new HashMap<>();

        props.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                BOOTSTRAP_SERVERS
        );

        props.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "user-profile-request-group"
        );

        props.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

        props.put(
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                true
        );

        props.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class
        );

        JacksonJsonDeserializer<ProfileRequestEvent>
                deserializer =
                new JacksonJsonDeserializer<>(
                        ProfileRequestEvent.class
                );

        deserializer.ignoreTypeHeaders();

        deserializer.trustedPackages(
                "ru.servicecompany.user.kafka.event"
        );

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean(name = "profileRequestKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<
            String,
            ProfileRequestEvent
            >
    profileRequestKafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<
                String,
                ProfileRequestEvent
                > factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(
                profileRequestConsumerFactory()
        );

        return factory;
    }
}