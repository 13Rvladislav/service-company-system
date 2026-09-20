package ru.servicecompany.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * HTTP-клиент для взаимодействия с user-service.
 */
@Configuration
public class RestClientConfig {

    @Bean(name = "userRestClient")
    public RestClient userRestClient() {
        return RestClient.builder()
                .baseUrl("http://localhost:8082")
                .build();
    }

}