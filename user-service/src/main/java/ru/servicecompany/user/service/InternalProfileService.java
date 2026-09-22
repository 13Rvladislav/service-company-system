package ru.servicecompany.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.servicecompany.user.common.exception.ApiException;
import ru.servicecompany.user.dto.request.CreateAdminProfileRequest;
import ru.servicecompany.user.dto.request.CreateDispatcherProfileRequest;
import ru.servicecompany.user.dto.request.CreateMasterProfileRequest;
import ru.servicecompany.user.entity.AdminProfile;
import ru.servicecompany.user.entity.DispatcherProfile;
import ru.servicecompany.user.entity.MasterProfile;
import ru.servicecompany.user.repository.AdminProfileRepository;
import ru.servicecompany.user.repository.DispatcherProfileRepository;
import ru.servicecompany.user.repository.MasterProfileRepository;

@Service
@RequiredArgsConstructor
public class InternalProfileService {

    private final MasterProfileRepository masterRepository;
    private final DispatcherProfileRepository dispatcherRepository;
    private final AdminProfileRepository adminRepository;

    /**
     * Создание профиля мастера.
     */
    public void createMaster(CreateMasterProfileRequest request) {

        if (masterRepository.existsByAuthUserId(request.getAuthUserId())) {
            throw new ApiException(HttpStatus.CONFLICT, "Профиль мастера уже существует");
        }

        MasterProfile profile = new MasterProfile();

        profile.setAuthUserId(request.getAuthUserId());
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setMiddleName(request.getMiddleName());
        profile.setPhone(request.getPhone());
        profile.setEmployeeNumber(request.getEmployeeNumber());
        profile.setSpecialization(request.getSpecialization());
        profile.setZoneId(request.getZoneId());

        masterRepository.save(profile);
    }

    /**
     * Создание профиля диспетчера.
     */
    public void createDispatcher(CreateDispatcherProfileRequest request) {

        if (dispatcherRepository.existsByAuthUserId(request.getAuthUserId())) {
            throw new ApiException(HttpStatus.CONFLICT, "Профиль диспетчера уже существует");
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
            throw new ApiException(HttpStatus.CONFLICT, "Профиль администратора уже существует");
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