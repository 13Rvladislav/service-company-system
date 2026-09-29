package ru.servicecompany.auth.kafka;

/**
 * Все Kafka-топики проекта.
 *
 * Зачем отдельный класс?
 * Чтобы нигде не писать строки вида
 * "user.profile.create" вручную.
 */
public final class KafkaTopics {

    private KafkaTopics() {
    }

    /**
     * Создание профиля пользователя.
     */
    public static final String USER_PROFILE_CREATE =
            "user.profile.create";

}