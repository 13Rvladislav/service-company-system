package ru.servicecompany.equipment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.servicecompany.equipment.entity.EquipmentType;

import java.util.UUID;

public interface EquipmentTypeRepository
        extends JpaRepository<EquipmentType, UUID> {

    boolean existsByNameIgnoreCase(String name);
}