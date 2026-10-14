
package ru.servicecompany.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
        name = "master_zones",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_master_zones_master_zone",
                        columnNames = {"master_id", "zone_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class MasterZone {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "master_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_master_zones_master")
    )
    private MasterProfile master;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "zone_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_master_zones_zone")
    )
    private Zone zone;

    public MasterZone(MasterProfile master, Zone zone) {
        this.master = master;
        this.zone = zone;
    }
}
