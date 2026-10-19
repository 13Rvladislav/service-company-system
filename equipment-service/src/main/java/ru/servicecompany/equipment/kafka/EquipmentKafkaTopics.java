package ru.servicecompany.equipment.kafka;

public final class EquipmentKafkaTopics {

    private EquipmentKafkaTopics() {
    }

    public static final String EQUIPMENT_REQUEST =
            "equipment.user.request";

    public static final String EQUIPMENT_RESPONSE =
            "equipment.user.response";

    public static final String EQUIPMENT_BY_ID_REQUEST =
            "equipment.by-id.request";

    public static final String EQUIPMENT_BY_ID_RESPONSE =
            "equipment.by-id.response";
}