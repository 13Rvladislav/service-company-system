package ru.servicecompany.equipment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.servicecompany.equipment.entity.UserEquipment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserEquipmentRepository
        extends JpaRepository<UserEquipment, UUID> {

    List<UserEquipment> findAllByAuthUserId(UUID authUserId);

    Optional<UserEquipment> findByIdAndAuthUserId(
            UUID id,
            UUID authUserId
    );
}