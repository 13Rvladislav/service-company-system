package ru.servicecompany.equipment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "equipment_catalog")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentCatalog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Тип оборудования.
     *
     * Например:
     * Газовый котёл
     * Газовая колонка
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "equipment_type_id",
            nullable = false
    )
    private EquipmentType equipmentType;

    /**
     * Производитель.
     *
     * Например:
     * Baxi
     * Ariston
     * Bosch
     */
    @Column(nullable = false, length = 100)
    private String manufacturer;

    /**
     * Модель оборудования.
     *
     * Например:
     * Eco Four
     * Clas X
     */
    @Column(nullable = false, length = 100)
    private String model;

    /**
     * Описание оборудования.
     */
    @Column(columnDefinition = "TEXT")
    private String description;
}