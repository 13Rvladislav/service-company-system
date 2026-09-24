package ru.servicecompany.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
        name = "houses",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"number", "street_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class House {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Номер дома
     * 25, 25А, 18/1...
     */
    @Column(nullable = false)
    private String number;

    /**
     * Улица
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "street_id", nullable = false)
    private Street street;
}