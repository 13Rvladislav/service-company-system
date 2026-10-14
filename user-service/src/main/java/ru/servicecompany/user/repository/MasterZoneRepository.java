
package ru.servicecompany.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.servicecompany.user.entity.MasterZone;

import java.util.List;
import java.util.UUID;

public interface MasterZoneRepository extends JpaRepository<MasterZone, UUID> {

    List<MasterZone> findAllByMaster_Id(UUID masterId);

    List<MasterZone> findAllByZone_Id(UUID zoneId);

    boolean existsByMaster_IdAndZone_Id(UUID masterId, UUID zoneId);

    void deleteByMaster_IdAndZone_Id(UUID masterId, UUID zoneId);

    @Modifying
    @Query("""
            DELETE FROM MasterZone mz
            WHERE mz.master.id = :masterId
            """)
    void deleteAllByMasterIdImmediately(@Param("masterId") UUID masterId);

    boolean existsByZone_Id(UUID zoneId);
}
