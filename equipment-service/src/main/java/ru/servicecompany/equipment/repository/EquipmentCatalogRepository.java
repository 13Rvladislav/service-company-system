package ru.servicecompany.equipment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.servicecompany.equipment.entity.EquipmentCatalog;

import java.util.UUID;

public interface EquipmentCatalogRepository
        extends JpaRepository<EquipmentCatalog, UUID> {
}