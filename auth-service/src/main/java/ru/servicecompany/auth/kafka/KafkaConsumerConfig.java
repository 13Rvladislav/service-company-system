package ru.servicecompany.auth.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import ru.servicecompany.auth.kafka.event.ProfileDeleteResponseEvent;
import ru.servicecompany.auth.kafka.event.ProfileFailedEvent;
import ru.servicecompany.auth.kafka.event.ProfileResponseEvent;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    private static final String BOOTSTRAP_SERVERS =
            "localhost:29092";

    private static final String GROUP_ID =
            "auth-service-group";

    /**
     * ============================================================
     * PROFILE FAILED
     * ============================================================
     */

    @Bean
    public ConsumerFactory<String, ProfileFailedEvent>
    profileFailedConsumerFactory() {

        Map<String, Object> properties =
                new HashMap<>();

        properties.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                BOOTSTRAP_SERVERS
        );

        properties.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                GROUP_ID
        );

        properties.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

        properties.put(
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                true
        );

        properties.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class
        );

        JacksonJsonDeserializer<ProfileFailedEvent>
                deserializer =
                new JacksonJsonDeserializer<>(
                        ProfileFailedEvent.class
                );

        deserializer.ignoreTypeHeaders();

        deserializer.trustedPackages(
                "ru.servicecompany.user.kafka.event",
                "ru.servicecompany.auth.kafka.event"
        );

        return new DefaultKafkaConsumerFactory<>(
                properties,
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean(name = "profileFailedKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<
            String,
            ProfileFailedEvent
            >
    profileFailedKafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<
                String,
                ProfileFailedEvent
                > factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(
                profileFailedConsumerFactory()
        );

        return factory;
    }

    /**
     * ============================================================
     * PROFILE RESPONSE
     * ============================================================
     */

    @Bean
    public ConsumerFactory<String, ProfileResponseEvent>
    profileResponseConsumerFactory() {

        Map<String, Object> properties =
                new HashMap<>();

        properties.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                BOOTSTRAP_SERVERS
        );

        properties.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "auth-profile-response-group"
        );

        properties.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

        properties.put(
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                true
        );

        properties.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class
        );

        JacksonJsonDeserializer<ProfileResponseEvent>
                deserializer =
                new JacksonJsonDeserializer<>(
                        ProfileResponseEvent.class
                );

        deserializer.ignoreTypeHeaders();

        deserializer.trustedPackages(
                "ru.servicecompany.user.kafka.event",
                "ru.servicecompany.auth.kafka.event"
        );

        return new DefaultKafkaConsumerFactory<>(
                properties,
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean(name = "profileResponseKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<
            String,
            ProfileResponseEvent
            >
    profileResponseKafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<
                String,
                ProfileResponseEvent
                > factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(
                profileResponseConsumerFactory()
        );

        return factory;
    }

    @Bean
    public ConsumerFactory<String, ProfileDeleteResponseEvent>
    profileDeleteResponseConsumerFactory() {

        Map<String, Object> properties =
                new HashMap<>();

        properties.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                BOOTSTRAP_SERVERS
        );

        properties.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "auth-profile-delete-response-group"
        );

        properties.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

        properties.put(
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                true
        );

        properties.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class
        );

        JacksonJsonDeserializer<ProfileDeleteResponseEvent>
                deserializer =
                new JacksonJsonDeserializer<>(
                        ProfileDeleteResponseEvent.class
                );

        deserializer.ignoreTypeHeaders();

        deserializer.trustedPackages(
                "ru.servicecompany.user.kafka.event",
                "ru.servicecompany.auth.kafka.event"
        );

        return new DefaultKafkaConsumerFactory<>(
                properties,
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean(name = "profileDeleteResponseKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<
            String,
            ProfileDeleteResponseEvent
            >
    profileDeleteResponseKafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<
                String,
                ProfileDeleteResponseEvent
                > factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(
                profileDeleteResponseConsumerFactory()
        );

        return factory;
    }
}