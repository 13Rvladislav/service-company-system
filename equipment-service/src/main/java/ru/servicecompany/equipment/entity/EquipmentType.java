package ru.servicecompany.equipment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "equipment_types",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_equipment_type_name",
                        columnNames = "name"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentType {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Название типа оборудования.
     *
     * Например:
     * Газовый котёл
     * Газовая колонка
     * Газовая плита
     */
    @Column(nullable = false, length = 100)
    private String name;
}