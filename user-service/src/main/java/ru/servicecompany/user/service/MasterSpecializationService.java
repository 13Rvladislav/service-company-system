
package ru.servicecompany.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.servicecompany.user.common.exception.ApiException;
import ru.servicecompany.user.dto.request.AssignSpecializationsRequest;
import ru.servicecompany.user.dto.request.CreateSpecializationRequest;
import ru.servicecompany.user.dto.request.UpdateSpecializationRequest;
import ru.servicecompany.user.dto.response.SpecializationResponse;
import ru.servicecompany.user.entity.MasterProfile;
import ru.servicecompany.user.entity.MasterSpecialization;
import ru.servicecompany.user.repository.MasterProfileRepository;
import ru.servicecompany.user.repository.MasterSpecializationRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MasterSpecializationService {

    private final MasterSpecializationRepository repository;
    private final MasterProfileRepository masterProfileRepository;

    @Transactional
    public SpecializationResponse create(CreateSpecializationRequest request) {
        String name = normalizeName(request.getName());

        if (repository.existsByNameIgnoreCase(name)) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Специализация с таким названием уже существует"
            );
        }

        Set<UUID> equipmentTypeIds = validateEquipmentTypeIds(
                request.getEquipmentTypeIds()
        );

        MasterSpecialization specialization = new MasterSpecialization();
        specialization.setName(name);
        specialization.setEquipmentTypeIds(equipmentTypeIds);

        return map(repository.save(specialization));
    }

    @Transactional(readOnly = true)
    public List<SpecializationResponse> getAll() {
        return repository.findAll()
                .stream()
                .map(this::map)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SpecializationResponse> getByEquipmentType(UUID equipmentTypeId) {
        return repository.findAll()
                .stream()
                .filter(specialization ->
                        specialization.getEquipmentTypeIds()
                                .contains(equipmentTypeId)
                )
                .map(this::map)
                .toList();
    }

    @Transactional(readOnly = true)
    public SpecializationResponse getById(UUID id) {
        return map(findById(id));
    }

    @Transactional
    public SpecializationResponse update(
            UUID id,
            UpdateSpecializationRequest request
    ) {
        MasterSpecialization specialization = findById(id);
        String name = normalizeName(request.getName());

        if (repository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Специализация с таким названием уже существует"
            );
        }

        Set<UUID> equipmentTypeIds = validateEquipmentTypeIds(
                request.getEquipmentTypeIds()
        );

        specialization.setName(name);
        specialization.setEquipmentTypeIds(equipmentTypeIds);

        return map(repository.save(specialization));
    }

    @Transactional
    public void delete(UUID id) {
        MasterSpecialization specialization = findById(id);
        repository.delete(specialization);
    }

    @Transactional(readOnly = true)
    public List<SpecializationResponse> getMasterSpecializations(UUID masterId) {
        MasterProfile master = masterProfileRepository.findById(masterId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Профиль мастера не найден"
                ));

        return master.getSpecializations()
                .stream()
                .map(this::map)
                .toList();
    }

    @Transactional
    public List<SpecializationResponse> assignToMaster(
            UUID masterId,
            AssignSpecializationsRequest request
    ) {
        MasterProfile master = masterProfileRepository.findById(masterId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Профиль мастера не найден"
                ));

        List<UUID> ids = request.getSpecializationIds();

        if (ids == null || ids.stream().anyMatch(id -> id == null)) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "ID специализации не может быть пустым"
            );
        }

        Set<UUID> uniqueIds = new HashSet<>(ids);

        List<MasterSpecialization> found =
                repository.findAllById(uniqueIds);

        if (found.size() != uniqueIds.size()) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Одна или несколько специализаций не найдены"
            );
        }

        master.setSpecializations(new HashSet<>(found));

        MasterProfile saved = masterProfileRepository.save(master);

        return saved.getSpecializations()
                .stream()
                .map(this::map)
                .toList();
    }

    private MasterSpecialization findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Специализация не найдена"
                ));
    }

    private Set<UUID> validateEquipmentTypeIds(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Выберите хотя бы один тип оборудования"
            );
        }

        if (ids.stream().anyMatch(id -> id == null)) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "ID типа оборудования не может быть пустым"
            );
        }

        return new HashSet<>(ids);
    }

    private String normalizeName(String name) {
        return name.trim();
    }

    private SpecializationResponse map(MasterSpecialization specialization) {
        return SpecializationResponse.builder()
                .id(specialization.getId())
                .name(specialization.getName())
                .equipmentTypeIds(
                        new HashSet<>(specialization.getEquipmentTypeIds())
                )
                .build();
    }
}
