package ru.servicecompany.auth.kafka;

/**
 * Все Kafka-топики проекта.
 */
public final class KafkaTopics {

    private KafkaTopics() {
    }

    /**
     * Создание профиля пользователя.
     */
    public static final String USER_PROFILE_CREATE =
            "user.profile.create";

    /**
     * Запрос профильных данных.
     */
    public static final String USER_PROFILE_REQUEST =
            "user.profile.request";

    /**
     * Ответ с профильными данными.
     */
    public static final String USER_PROFILE_RESPONSE =
            "user.profile.response";
}