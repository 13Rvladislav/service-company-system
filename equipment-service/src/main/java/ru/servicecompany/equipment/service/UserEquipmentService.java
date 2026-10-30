
package ru.servicecompany.equipment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.servicecompany.equipment.common.exception.ApiException;
import ru.servicecompany.equipment.dto.request.UserEquipmentRequest;
import ru.servicecompany.equipment.dto.response.UserEquipmentResponse;
import ru.servicecompany.equipment.entity.EquipmentCatalog;
import ru.servicecompany.equipment.entity.UserEquipment;
import ru.servicecompany.equipment.repository.EquipmentCatalogRepository;
import ru.servicecompany.equipment.repository.UserEquipmentRepository;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserEquipmentService {

    private final UserEquipmentRepository userEquipmentRepository;
    private final EquipmentCatalogRepository equipmentCatalogRepository;
    private final AuditService auditService;

    /**
     * Получить оборудование текущего пользователя.
     */
    @Transactional(readOnly = true)
    public List<UserEquipmentResponse> getMyEquipment(UUID authUserId) {

        return userEquipmentRepository
                .findAllByAuthUserId(authUserId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Получить оборудование пользователя по ID.
     */
    @Transactional(readOnly = true)
    public UserEquipmentResponse getMyEquipmentById(
            UUID authUserId,
            UUID equipmentId
    ) {

        UserEquipment userEquipment =
                userEquipmentRepository.findByIdAndAuthUserId(
                        equipmentId,
                        authUserId
                ).orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Оборудование не найдено"
                ));

        return mapToResponse(userEquipment);
    }

    /**
     * Добавить оборудование клиента.
     */
    public UserEquipmentResponse addEquipment(
            UUID authUserId,
            UserEquipmentRequest request
    ) {

        EquipmentCatalog equipmentCatalog =
                getEquipmentCatalog(request.getEquipmentCatalogId());

        UserEquipment userEquipment =
                UserEquipment.builder()
                        .authUserId(authUserId)
                        .equipmentCatalog(equipmentCatalog)
                        .serialNumber(normalizeSerialNumber(
                                request.getSerialNumber()
                        ))
                        .installationDate(request.getInstallationDate())
                        .build();

        UserEquipment saved =
                userEquipmentRepository.save(userEquipment);

        auditService.logSuccess(
                "USER_EQUIPMENT_ADDED",
                "USER_EQUIPMENT",
                saved.getId(),
                "operation=CREATE"
                        + ";equipmentCatalogId=" + equipmentCatalog.getId()
                        + ";equipmentTypeId="
                        + equipmentCatalog.getEquipmentType().getId()
        );

        log.info(
                "BUSINESS_EVENT event=USER_EQUIPMENT_ADDED authUserId={} equipmentId={} catalogId={}",
                authUserId,
                saved.getId(),
                equipmentCatalog.getId()
        );

        return mapToResponse(saved);
    }

    /**
     * Обновить оборудование клиента.
     */
    public UserEquipmentResponse updateEquipment(
            UUID authUserId,
            UUID equipmentId,
            UserEquipmentRequest request
    ) {

        UserEquipment userEquipment =
                userEquipmentRepository.findByIdAndAuthUserId(
                        equipmentId,
                        authUserId
                ).orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Оборудование не найдено"
                ));

        UUID previousCatalogId =
                userEquipment.getEquipmentCatalog().getId();

        EquipmentCatalog equipmentCatalog =
                getEquipmentCatalog(request.getEquipmentCatalogId());

        userEquipment.setEquipmentCatalog(equipmentCatalog);

        userEquipment.setSerialNumber(
                normalizeSerialNumber(request.getSerialNumber())
        );

        userEquipment.setInstallationDate(
                request.getInstallationDate()
        );

        UserEquipment updated =
                userEquipmentRepository.save(userEquipment);

        auditService.logSuccess(
                "USER_EQUIPMENT_UPDATED",
                "USER_EQUIPMENT",
                updated.getId(),
                "operation=UPDATE"
                        + ";previousCatalogId=" + previousCatalogId
                        + ";newCatalogId=" + equipmentCatalog.getId()
        );

        log.info(
                "BUSINESS_EVENT event=USER_EQUIPMENT_UPDATED authUserId={} equipmentId={} previousCatalogId={} newCatalogId={}",
                authUserId,
                updated.getId(),
                previousCatalogId,
                equipmentCatalog.getId()
        );

        return mapToResponse(updated);
    }

    /**
     * Удалить оборудование клиента.
     */
    public void deleteEquipment(
            UUID authUserId,
            UUID equipmentId
    ) {

        UserEquipment userEquipment =
                userEquipmentRepository.findByIdAndAuthUserId(
                        equipmentId,
                        authUserId
                ).orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Оборудование не найдено"
                ));

        UUID catalogId =
                userEquipment.getEquipmentCatalog().getId();

        userEquipmentRepository.delete(userEquipment);

        auditService.logSuccess(
                "USER_EQUIPMENT_DELETED",
                "USER_EQUIPMENT",
                equipmentId,
                "operation=DELETE;equipmentCatalogId=" + catalogId
        );

        log.info(
                "BUSINESS_EVENT event=USER_EQUIPMENT_DELETED authUserId={} equipmentId={} catalogId={}",
                authUserId,
                equipmentId,
                catalogId
        );
    }

    /**
     * Получить оборудование из каталога.
     */
    private EquipmentCatalog getEquipmentCatalog(
            UUID equipmentCatalogId
    ) {

        return equipmentCatalogRepository
                .findById(equipmentCatalogId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Оборудование из справочника с id "
                                + equipmentCatalogId
                                + " не найдено"
                ));
    }

    /**
     * Entity -> Response DTO.
     */
    private UserEquipmentResponse mapToResponse(
            UserEquipment userEquipment
    ) {

        EquipmentCatalog catalog =
                userEquipment.getEquipmentCatalog();

        return UserEquipmentResponse.builder()
                .id(userEquipment.getId())
                .equipmentCatalogId(catalog.getId())
                .equipmentTypeId(catalog.getEquipmentType().getId())
                .equipmentTypeName(catalog.getEquipmentType().getName())
                .manufacturer(catalog.getManufacturer())
                .model(catalog.getModel())
                .description(catalog.getDescription())
                .serialNumber(userEquipment.getSerialNumber())
                .installationDate(userEquipment.getInstallationDate())
                .build();
    }

    /**
     * Нормализовать серийный номер.
     */
    private String normalizeSerialNumber(String serialNumber) {

        if (serialNumber == null || serialNumber.isBlank()) {
            return null;
        }

        return serialNumber.trim();
    }
}
