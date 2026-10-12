
package ru.servicecompany.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
        name = "master_specializations",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_master_specialization_name_equipment_type",
                columnNames = {"name", "equipment_type_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class MasterSpecialization {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    /**
     * ID типа оборудования из equipment-service.
     * Это идентификатор другого микросервиса,
     * поэтому JPA-связь с его Entity не создаём.
     */
    @Column(name = "equipment_type_id", nullable = false)
    private UUID equipmentTypeId;
}
