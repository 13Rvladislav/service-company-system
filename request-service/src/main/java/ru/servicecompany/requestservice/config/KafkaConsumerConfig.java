package ru.servicecompany.requestservice.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import ru.servicecompany.requestservice.kafka.event.ProfileResponseEvent;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, ProfileResponseEvent>
    profileResponseConsumerFactory(Environment environment) {

        Map<String, Object> properties = new HashMap<>();

        properties.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                environment.getRequiredProperty(
                        "spring.kafka.bootstrap-servers"
                )
        );

        properties.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "request-service-profile-response-group"
        );

        properties.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "latest"
        );

        properties.put(
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                true
        );

        JacksonJsonDeserializer<ProfileResponseEvent> jsonDeserializer =
                new JacksonJsonDeserializer<>(
                        ProfileResponseEvent.class
                );

        jsonDeserializer.ignoreTypeHeaders();

        return new DefaultKafkaConsumerFactory<>(
                properties,
                new StringDeserializer(),
                jsonDeserializer
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


}