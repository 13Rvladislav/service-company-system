package ru.servicecompany.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
        name = "streets",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"name", "city_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class Street {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Название улицы
     */
    @Column(nullable = false)
    private String name;

    /**
     * Город
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    /**
     * Зона обслуживания
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;
}