
package ru.servicecompany.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.servicecompany.user.entity.MasterSpecialization;

import java.util.List;
import java.util.UUID;

public interface MasterSpecializationRepository
        extends JpaRepository<MasterSpecialization, UUID> {

    boolean existsByNameIgnoreCaseAndEquipmentTypeId(
            String name,
            UUID equipmentTypeId
    );

    boolean existsByNameIgnoreCaseAndEquipmentTypeIdAndIdNot(
            String name,
            UUID equipmentTypeId,
            UUID id
    );

    List<MasterSpecialization> findAllByEquipmentTypeId(UUID equipmentTypeId);
}
