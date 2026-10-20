package ru.servicecompany.requestservice.kafka;

public final class KafkaTopics {

    private KafkaTopics() {
    }

    public static final String USER_PROFILE_REQUEST =
            "user.profile.request";

    public static final String USER_PROFILE_RESPONSE =
            "user.profile.response";

    public static final String EQUIPMENT_BY_ID_REQUEST =
            "equipment.by-id.request";

    public static final String EQUIPMENT_BY_ID_RESPONSE =
            "equipment.by-id.response";

    public static final String ADDRESS_BY_ID_REQUEST =
            "address.by-id.request";

    public static final String ADDRESS_BY_ID_RESPONSE =
            "address.by-id.response";

    public static final String MASTER_BY_ID_REQUEST =
            "master.by-id.request";

    public static final String MASTER_BY_ID_RESPONSE =
            "master.by-id.response";
}