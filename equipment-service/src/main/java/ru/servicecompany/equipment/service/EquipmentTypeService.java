package ru.servicecompany.equipment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.servicecompany.equipment.common.exception.ApiException;
import ru.servicecompany.equipment.dto.request.EquipmentTypeRequest;
import ru.servicecompany.equipment.dto.response.EquipmentTypeResponse;
import ru.servicecompany.equipment.entity.EquipmentType;
import ru.servicecompany.equipment.repository.EquipmentTypeRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class EquipmentTypeService {

    private final EquipmentTypeRepository equipmentTypeRepository;

    /**
     * Получить все типы оборудования.
     */
    @Transactional(readOnly = true)
    public List<EquipmentTypeResponse> getAll() {

        return equipmentTypeRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Получить тип оборудования по ID.
     */
    @Transactional(readOnly = true)
    public EquipmentTypeResponse getById(UUID id) {

        EquipmentType equipmentType =
                equipmentTypeRepository.findById(id)
                        .orElseThrow(() -> new ApiException(
                                HttpStatus.NOT_FOUND,
                                "Тип оборудования с id " + id + " не найден"
                        ));

        return mapToResponse(equipmentType);
    }

    /**
     * Создать новый тип оборудования.
     */
    public EquipmentTypeResponse create(
            EquipmentTypeRequest request
    ) {

        String name = request.getName().trim();

        if (equipmentTypeRepository.existsByNameIgnoreCase(name)) {

            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Тип оборудования с названием '"
                            + name
                            + "' уже существует"
            );
        }

        EquipmentType equipmentType =
                EquipmentType.builder()
                        .name(name)
                        .build();

        EquipmentType saved =
                equipmentTypeRepository.save(equipmentType);

        return mapToResponse(saved);
    }

    /**
     * Обновить тип оборудования.
     */
    public EquipmentTypeResponse update(
            UUID id,
            EquipmentTypeRequest request
    ) {

        EquipmentType equipmentType =
                equipmentTypeRepository.findById(id)
                        .orElseThrow(() -> new ApiException(
                                HttpStatus.NOT_FOUND,
                                "Тип оборудования с id "
                                        + id
                                        + " не найден"
                        ));

        String name = request.getName().trim();

        if (!equipmentType.getName().equalsIgnoreCase(name)
                && equipmentTypeRepository.existsByNameIgnoreCase(name)) {

            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Тип оборудования с названием '"
                            + name
                            + "' уже существует"
            );
        }

        equipmentType.setName(name);

        EquipmentType updated =
                equipmentTypeRepository.save(equipmentType);

        return mapToResponse(updated);
    }

    /**
     * Удалить тип оборудования.
     */
    public void delete(UUID id) {

        EquipmentType equipmentType =
                equipmentTypeRepository.findById(id)
                        .orElseThrow(() -> new ApiException(
                                HttpStatus.NOT_FOUND,
                                "Тип оборудования с id "
                                        + id
                                        + " не найден"
                        ));

        equipmentTypeRepository.delete(equipmentType);
    }

    private EquipmentTypeResponse mapToResponse(
            EquipmentType equipmentType
    ) {

        return EquipmentTypeResponse.builder()
                .id(equipmentType.getId())
                .name(equipmentType.getName())
                .build();
    }
}