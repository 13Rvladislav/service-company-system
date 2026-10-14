
package ru.servicecompany.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.servicecompany.user.entity.MasterZone;

import java.util.List;
import java.util.UUID;

public interface MasterZoneRepository extends JpaRepository<MasterZone, UUID> {

    List<MasterZone> findAllByMaster_Id(UUID masterId);

    List<MasterZone> findAllByZone_Id(UUID zoneId);

    boolean existsByMaster_IdAndZone_Id(UUID masterId, UUID zoneId);

    void deleteByMaster_IdAndZone_Id(UUID masterId, UUID zoneId);

    void deleteAllByMaster_Id(UUID masterId);

    boolean existsByZone_Id(UUID zoneId);
}
