package ru.servicecompany.equipment.kafka;

public final class EquipmentKafkaTopics {

    private EquipmentKafkaTopics() {
    }

    public static final String EQUIPMENT_REQUEST =
            "equipment.user.request";

    public static final String EQUIPMENT_RESPONSE =
            "equipment.user.response";
}