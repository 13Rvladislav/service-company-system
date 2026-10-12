package ru.servicecompany.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.servicecompany.user.common.exception.ApiException;
import ru.servicecompany.user.dto.request.CreateAdminProfileRequest;
import ru.servicecompany.user.dto.request.CreateDispatcherProfileRequest;
import ru.servicecompany.user.dto.request.CreateMasterProfileRequest;
import ru.servicecompany.user.entity.AdminProfile;
import ru.servicecompany.user.entity.DispatcherProfile;
import ru.servicecompany.user.entity.MasterProfile;
import ru.servicecompany.user.entity.MasterSpecialization;
import ru.servicecompany.user.repository.AdminProfileRepository;
import ru.servicecompany.user.repository.DispatcherProfileRepository;
import ru.servicecompany.user.repository.MasterProfileRepository;
import ru.servicecompany.user.repository.MasterSpecializationRepository;
import ru.servicecompany.user.repository.ZoneRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InternalProfileService {

    private final MasterProfileRepository masterRepository;
    private final DispatcherProfileRepository dispatcherRepository;
    private final AdminProfileRepository adminRepository;
    private final ZoneRepository zoneRepository;
    private final MasterSpecializationRepository specializationRepository;

    /**
     * Создание профиля мастера.
     */
    @Transactional
    public void createMaster(CreateMasterProfileRequest request) {

        if (masterRepository.existsByAuthUserId(request.getAuthUserId())) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Профиль мастера уже существует"
            );
        }

        // Проверяем существование зоны.
        if (request.getZoneId() != null
                && !zoneRepository.existsById(request.getZoneId())) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Указанная зона не существует"
            );
        }

        // Получаем специализации по их UUID.
        List<UUID> specializationIds = request.getSpecializationIds();

        Set<MasterSpecialization> specializations = new HashSet<>();

        if (specializationIds != null && !specializationIds.isEmpty()) {

            if (specializationIds.stream().anyMatch(id -> id == null)) {
                throw new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "ID специализации не может быть пустым"
                );
            }

            Set<UUID> uniqueIds = new HashSet<>(specializationIds);

            List<MasterSpecialization> foundSpecializations =
                    specializationRepository.findAllById(uniqueIds);

            if (foundSpecializations.size() != uniqueIds.size()) {
                throw new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "Одна или несколько специализаций не найдены"
                );
            }

            specializations.addAll(foundSpecializations);
        }

        MasterProfile profile = new MasterProfile();

        profile.setAuthUserId(request.getAuthUserId());
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setMiddleName(request.getMiddleName());
        profile.setPhone(request.getPhone());
        profile.setEmployeeNumber(request.getEmployeeNumber());
        profile.setSpecializations(specializations);
        profile.setZoneId(request.getZoneId());

        masterRepository.save(profile);
    }

    /**
     * Создание профиля диспетчера.
     */
    public void createDispatcher(CreateDispatcherProfileRequest request) {

        if (dispatcherRepository.existsByAuthUserId(request.getAuthUserId())) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Профиль диспетчера уже существует"
            );
        }

        DispatcherProfile profile = new DispatcherProfile();

        profile.setAuthUserId(request.getAuthUserId());
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setMiddleName(request.getMiddleName());
        profile.setPhone(request.getPhone());
        profile.setEmployeeNumber(request.getEmployeeNumber());
        profile.setDepartment(request.getDepartment());

        dispatcherRepository.save(profile);
    }

    /**
     * Создание профиля администратора.
     */
    public void createAdmin(CreateAdminProfileRequest request) {

        if (adminRepository.existsByAuthUserId(request.getAuthUserId())) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Профиль администратора уже существует"
            );
        }

        AdminProfile profile = new AdminProfile();

        profile.setAuthUserId(request.getAuthUserId());
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setMiddleName(request.getMiddleName());
        profile.setPhone(request.getPhone());
        profile.setEmployeeNumber(request.getEmployeeNumber());
        profile.setPosition(request.getPosition());

        adminRepository.save(profile);
    }
}
