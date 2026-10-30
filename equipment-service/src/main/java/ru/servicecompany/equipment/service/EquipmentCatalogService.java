
package ru.servicecompany.equipment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.servicecompany.equipment.common.exception.ApiException;
import ru.servicecompany.equipment.dto.request.EquipmentCatalogRequest;
import ru.servicecompany.equipment.dto.response.EquipmentCatalogResponse;
import ru.servicecompany.equipment.entity.EquipmentCatalog;
import ru.servicecompany.equipment.entity.EquipmentType;
import ru.servicecompany.equipment.repository.EquipmentCatalogRepository;
import ru.servicecompany.equipment.repository.EquipmentTypeRepository;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EquipmentCatalogService {

    private final EquipmentCatalogRepository equipmentCatalogRepository;
    private final EquipmentTypeRepository equipmentTypeRepository;

    /**
     * Получить весь каталог оборудования.
     */
    @Transactional(readOnly = true)
    public List<EquipmentCatalogResponse> getAll() {

        return equipmentCatalogRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Получить оборудование из каталога по ID.
     */
    @Transactional(readOnly = true)
    public EquipmentCatalogResponse getById(UUID id) {

        EquipmentCatalog equipment =
                equipmentCatalogRepository.findById(id)
                        .orElseThrow(() -> new ApiException(
                                HttpStatus.NOT_FOUND,
                                "Оборудование с id "
                                        + id
                                        + " не найдено"
                        ));

        return mapToResponse(equipment);
    }

    /**
     * Добавить оборудование в каталог.
     */
    public EquipmentCatalogResponse create(
            EquipmentCatalogRequest request
    ) {

        EquipmentType equipmentType =
                getEquipmentType(request.getEquipmentTypeId());

        EquipmentCatalog equipment =
                EquipmentCatalog.builder()
                        .equipmentType(equipmentType)
                        .manufacturer(
                                request.getManufacturer().trim()
                        )
                        .model(
                                request.getModel().trim()
                        )
                        .description(
                                normalizeDescription(
                                        request.getDescription()
                                )
                        )
                        .build();

        EquipmentCatalog saved =
                equipmentCatalogRepository.save(equipment);

        log.info(
                "BUSINESS_EVENT event=EQUIPMENT_CATALOG_CREATED equipmentId={} equipmentTypeId={} manufacturer={} model={}",
                saved.getId(),
                equipmentType.getId(),
                saved.getManufacturer(),
                saved.getModel()
        );

        return mapToResponse(saved);
    }

    /**
     * Обновить оборудование в каталоге.
     */
    public EquipmentCatalogResponse update(
            UUID id,
            EquipmentCatalogRequest request
    ) {

        EquipmentCatalog equipment =
                equipmentCatalogRepository.findById(id)
                        .orElseThrow(() -> new ApiException(
                                HttpStatus.NOT_FOUND,
                                "Оборудование с id "
                                        + id
                                        + " не найдено"
                        ));

        EquipmentType equipmentType =
                getEquipmentType(request.getEquipmentTypeId());

        UUID previousEquipmentTypeId =
                equipment.getEquipmentType().getId();

        equipment.setEquipmentType(equipmentType);

        equipment.setManufacturer(
                request.getManufacturer().trim()
        );

        equipment.setModel(
                request.getModel().trim()
        );

        equipment.setDescription(
                normalizeDescription(
                        request.getDescription()
                )
        );

        EquipmentCatalog updated =
                equipmentCatalogRepository.save(equipment);

        log.info(
                "BUSINESS_EVENT event=EQUIPMENT_CATALOG_UPDATED equipmentId={} previousEquipmentTypeId={} newEquipmentTypeId={} manufacturer={} model={}",
                updated.getId(),
                previousEquipmentTypeId,
                equipmentType.getId(),
                updated.getManufacturer(),
                updated.getModel()
        );

        return mapToResponse(updated);
    }

    /**
     * Удалить оборудование из каталога.
     */
    public void delete(UUID id) {

        EquipmentCatalog equipment =
                equipmentCatalogRepository.findById(id)
                        .orElseThrow(() -> new ApiException(
                                HttpStatus.NOT_FOUND,
                                "Оборудование с id "
                                        + id
                                        + " не найдено"
                        ));

        UUID equipmentTypeId = equipment.getEquipmentType().getId();
        String manufacturer = equipment.getManufacturer();
        String model = equipment.getModel();

        equipmentCatalogRepository.delete(equipment);

        log.info(
                "BUSINESS_EVENT event=EQUIPMENT_CATALOG_DELETED equipmentId={} equipmentTypeId={} manufacturer={} model={}",
                id,
                equipmentTypeId,
                manufacturer,
                model
        );
    }

    /**
     * Получить тип оборудования.
     */
    private EquipmentType getEquipmentType(UUID id) {

        return equipmentTypeRepository.findById(id)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Тип оборудования с id "
                                + id
                                + " не найден"
                ));
    }

    /**
     * Преобразовать Entity в Response DTO.
     */
    private EquipmentCatalogResponse mapToResponse(
            EquipmentCatalog equipment
    ) {

        return EquipmentCatalogResponse.builder()
                .id(equipment.getId())
                .equipmentTypeId(
                        equipment.getEquipmentType().getId()
                )
                .equipmentTypeName(
                        equipment.getEquipmentType().getName()
                )
                .manufacturer(
                        equipment.getManufacturer()
                )
                .model(
                        equipment.getModel()
                )
                .description(
                        equipment.getDescription()
                )
                .build();
    }

    /**
     * Нормализовать описание.
     */
    private String normalizeDescription(
            String description
    ) {

        if (description == null || description.isBlank()) {
            return null;
        }

        return description.trim();
    }
}
