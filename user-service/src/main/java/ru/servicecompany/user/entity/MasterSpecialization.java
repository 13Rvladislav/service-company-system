
package ru.servicecompany.user.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
        name = "master_specializations",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_master_specialization_name",
                columnNames = "name"
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
     * ID типов оборудования из equipment-service.
     *
     * Это UUID внешнего микросервиса, поэтому связь с его
     * Entity не создаём. ID хранятся в отдельной таблице.
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "specialization_equipment_types",
            joinColumns = @JoinColumn(name = "specialization_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_specialization_equipment_type",
                    columnNames = {"specialization_id", "equipment_type_id"}
            )
    )
    @Column(name = "equipment_type_id", nullable = false)
    private Set<UUID> equipmentTypeIds = new HashSet<>();
}
