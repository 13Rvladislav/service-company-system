package ru.servicecompany.equipment.service;

import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
@Transactional
public class UserEquipmentService {

    private final UserEquipmentRepository userEquipmentRepository;
    private final EquipmentCatalogRepository equipmentCatalogRepository;

    @Transactional(readOnly = true)
    public List<UserEquipmentResponse> getMyEquipment(
            UUID authUserId
    ) {
        return userEquipmentRepository
                .findAllByAuthUserId(authUserId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserEquipmentResponse getMyEquipmentById(
            UUID authUserId,
            UUID equipmentId
    ) {
        UserEquipment userEquipment =
                userEquipmentRepository
                        .findByIdAndAuthUserId(
                                equipmentId,
                                authUserId
                        )
                        .orElseThrow(() -> new ApiException(
                                HttpStatus.NOT_FOUND,
                                "Оборудование не найдено"
                        ));

        return mapToResponse(userEquipment);
    }

    public UserEquipmentResponse addEquipment(
            UUID authUserId,
            UserEquipmentRequest request
    ) {
        EquipmentCatalog equipmentCatalog =
                getEquipmentCatalog(
                        request.getEquipmentCatalogId()
                );

        UserEquipment userEquipment =
                UserEquipment.builder()
                        .authUserId(authUserId)
                        .equipmentCatalog(equipmentCatalog)
                        .serialNumber(normalizeSerialNumber(
                                request.getSerialNumber()
                        ))
                        .installationDate(
                                request.getInstallationDate()
                        )
                        .build();

        UserEquipment saved =
                userEquipmentRepository.save(userEquipment);

        return mapToResponse(saved);
    }

    public UserEquipmentResponse updateEquipment(
            UUID authUserId,
            UUID equipmentId,
            UserEquipmentRequest request
    ) {
        UserEquipment userEquipment =
                userEquipmentRepository
                        .findByIdAndAuthUserId(
                                equipmentId,
                                authUserId
                        )
                        .orElseThrow(() -> new ApiException(
                                HttpStatus.NOT_FOUND,
                                "Оборудование не найдено"
                        ));

        EquipmentCatalog equipmentCatalog =
                getEquipmentCatalog(
                        request.getEquipmentCatalogId()
                );

        userEquipment.setEquipmentCatalog(
                equipmentCatalog
        );

        userEquipment.setSerialNumber(
                normalizeSerialNumber(
                        request.getSerialNumber()
                )
        );

        userEquipment.setInstallationDate(
                request.getInstallationDate()
        );

        UserEquipment updated =
                userEquipmentRepository.save(
                        userEquipment
                );

        return mapToResponse(updated);
    }

    public void deleteEquipment(
            UUID authUserId,
            UUID equipmentId
    ) {
        UserEquipment userEquipment =
                userEquipmentRepository
                        .findByIdAndAuthUserId(
                                equipmentId,
                                authUserId
                        )
                        .orElseThrow(() -> new ApiException(
                                HttpStatus.NOT_FOUND,
                                "Оборудование не найдено"
                        ));

        userEquipmentRepository.delete(userEquipment);
    }

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

    private UserEquipmentResponse mapToResponse(
            UserEquipment userEquipment
    ) {
        EquipmentCatalog catalog =
                userEquipment.getEquipmentCatalog();

        return UserEquipmentResponse.builder()
                .id(userEquipment.getId())
                .equipmentCatalogId(catalog.getId())
                .equipmentTypeId(
                        catalog.getEquipmentType().getId()
                )
                .equipmentTypeName(
                        catalog.getEquipmentType().getName()
                )
                .manufacturer(
                        catalog.getManufacturer()
                )
                .model(
                        catalog.getModel()
                )
                .description(
                        catalog.getDescription()
                )
                .serialNumber(
                        userEquipment.getSerialNumber()
                )
                .installationDate(
                        userEquipment.getInstallationDate()
                )
                .build();
    }

    private String normalizeSerialNumber(
            String serialNumber
    ) {
        if (serialNumber == null || serialNumber.isBlank()) {
            return null;
        }

        return serialNumber.trim();
    }
}