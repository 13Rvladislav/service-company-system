package ru.servicecompany.user.kafka;

/**
 * Все Kafka Topic проекта.
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

    /**
     * Удаление профиля.
     */
    public static final String USER_PROFILE_DELETE =
            "user.profile.delete";

    /**
     * Ответ удаления профиля.
     */
    public static final String USER_PROFILE_DELETE_RESPONSE =
            "user.profile.delete.response";

    /**
     * Обновление профиля.
     */
    public static final String USER_PROFILE_UPDATE =
            "user.profile.update";

    /**
     * Ответ обновления профиля.
     */
    public static final String USER_PROFILE_UPDATE_RESPONSE =
            "user.profile.update.response";

    public static final String ADDRESS_BY_ID_REQUEST =
            "address.by-id.request";

    public static final String ADDRESS_BY_ID_RESPONSE =
            "address.by-id.response";

    public static final String MASTER_BY_ID_REQUEST =
            "master.by-id.request";

    public static final String MASTER_BY_ID_RESPONSE =
            "master.by-id.response";
}