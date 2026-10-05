package ru.servicecompany.equipment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "user_equipment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEquipment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * ID пользователя из auth-service.
     */
    @Column(nullable = false)
    private UUID authUserId;

    /**
     * Оборудование из справочника.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipment_catalog_id", nullable = false)
    private EquipmentCatalog equipmentCatalog;

    /**
     * Серийный номер конкретного экземпляра.
     */
    @Column(length = 100)
    private String serialNumber;

    /**
     * Дата установки оборудования.
     */
    private LocalDate installationDate;
}