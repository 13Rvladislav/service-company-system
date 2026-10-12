
package ru.servicecompany.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.servicecompany.user.entity.MasterSpecialization;

import java.util.UUID;

public interface MasterSpecializationRepository
        extends JpaRepository<MasterSpecialization, UUID> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);
}
