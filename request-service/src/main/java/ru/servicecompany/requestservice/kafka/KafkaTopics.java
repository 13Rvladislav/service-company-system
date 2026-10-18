package ru.servicecompany.requestservice.kafka;

public final class KafkaTopics {

    private KafkaTopics() {
    }

    public static final String USER_PROFILE_REQUEST =
            "user.profile.request";

    public static final String USER_PROFILE_RESPONSE =
            "user.profile.response";
}