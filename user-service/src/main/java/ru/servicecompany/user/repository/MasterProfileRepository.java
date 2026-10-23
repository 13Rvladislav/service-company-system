package ru.servicecompany.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.servicecompany.user.entity.MasterProfile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MasterProfileRepository
        extends JpaRepository<MasterProfile, UUID> {

    Optional<MasterProfile> findByAuthUserId(UUID authUserId);

    boolean existsByAuthUserId(UUID authUserId);

    @Query("""
            select distinct master.id
            from MasterProfile master
            join master.specializations specialization
            join specialization.equipmentTypeIds equipmentTypeId
            where equipmentTypeId = :equipmentTypeId
            """)
    List<UUID> findManualAssignmentCandidates(
            @Param("equipmentTypeId") UUID equipmentTypeId
    );

    @Query("""
            select distinct master.id
            from MasterProfile master
            join MasterZone masterZone
                on masterZone.master = master
            join master.specializations specialization
            join specialization.equipmentTypeIds equipmentTypeId
            where masterZone.zone.id = :zoneId
              and equipmentTypeId = :equipmentTypeId
            """)
    List<UUID> findAssignmentCandidates(
            @Param("zoneId") UUID zoneId,
            @Param("equipmentTypeId") UUID equipmentTypeId
    );
}