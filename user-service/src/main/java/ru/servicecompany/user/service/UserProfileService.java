package ru.servicecompany.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.servicecompany.user.common.exception.ApiException;
import ru.servicecompany.user.dto.request.CreateUserProfileRequest;
import ru.servicecompany.user.dto.request.UpdateUserProfileRequest;
import ru.servicecompany.user.dto.response.UserProfileResponse;
import ru.servicecompany.user.entity.AdminProfile;
import ru.servicecompany.user.entity.DispatcherProfile;
import ru.servicecompany.user.entity.MasterProfile;
import ru.servicecompany.user.entity.UserProfile;
import ru.servicecompany.user.repository.AdminProfileRepository;
import ru.servicecompany.user.repository.DispatcherProfileRepository;
import ru.servicecompany.user.repository.MasterProfileRepository;
import ru.servicecompany.user.repository.UserProfileRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userRepository;
    private final MasterProfileRepository masterRepository;
    private final DispatcherProfileRepository dispatcherRepository;
    private final AdminProfileRepository adminRepository;

    /**
     * Создание профиля клиента.
     */
    public UserProfileResponse create(CreateUserProfileRequest request) {

        if (userRepository.existsByAuthUserId(request.getAuthUserId())) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Профиль уже существует"
            );
        }

        UserProfile profile = UserProfile.builder()
                .authUserId(request.getAuthUserId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .middleName(request.getMiddleName())
                .phone(request.getPhone())
                .build();

        return map(userRepository.save(profile), "CLIENT");
    }

    /**
     * Получить профиль текущего пользователя.
     */
    public UserProfileResponse getCurrentProfile(UUID userId, String role) {

        return switch (role) {

            case "CLIENT" -> {
                UserProfile p = userRepository.findByAuthUserId(userId)
                        .orElseThrow(() -> new ApiException(
                                HttpStatus.NOT_FOUND,
                                "Профиль пользователя не найден"
                        ));

                yield map(p, role);
            }

            case "ENGINEER" -> {
                MasterProfile p = masterRepository.findByAuthUserId(userId)
                        .orElseThrow(() -> new ApiException(
                                HttpStatus.NOT_FOUND,
                                "Профиль мастера не найден"
                        ));

                yield UserProfileResponse.builder()
                        .id(p.getId())
                        .authUserId(p.getAuthUserId())
                        .firstName(p.getFirstName())
                        .lastName(p.getLastName())
                        .middleName(p.getMiddleName())
                        .phone(p.getPhone())
                        .employeeNumber(p.getEmployeeNumber())
                        .specialization(p.getSpecialization())
                        .zoneId(p.getZoneId())
                        .status(p.getStatus().name())
                        .role(role)
                        .build();
            }

            case "DISPATCHER" -> {
                DispatcherProfile p = dispatcherRepository.findByAuthUserId(userId)
                        .orElseThrow(() -> new ApiException(
                                HttpStatus.NOT_FOUND,
                                "Профиль диспетчера не найден"
                        ));

                yield UserProfileResponse.builder()
                        .id(p.getId())
                        .authUserId(p.getAuthUserId())
                        .firstName(p.getFirstName())
                        .lastName(p.getLastName())
                        .middleName(p.getMiddleName())
                        .phone(p.getPhone())
                        .employeeNumber(p.getEmployeeNumber())
                        .department(p.getDepartment())
                        .role(role)
                        .build();
            }

            case "ADMIN" -> {
                AdminProfile p = adminRepository.findByAuthUserId(userId)
                        .orElseThrow(() -> new ApiException(
                                HttpStatus.NOT_FOUND,
                                "Профиль администратора не найден"
                        ));

                yield UserProfileResponse.builder()
                        .id(p.getId())
                        .authUserId(p.getAuthUserId())
                        .firstName(p.getFirstName())
                        .lastName(p.getLastName())
                        .middleName(p.getMiddleName())
                        .phone(p.getPhone())
                        .employeeNumber(p.getEmployeeNumber())
                        .position(p.getPosition())
                        .role(role)
                        .build();
            }

            default -> throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Неизвестная роль"
            );
        };
    }

    /**
     * Обновление профиля клиента.
     */
    public UserProfileResponse updateCurrentProfile(
            UUID userId,
            UpdateUserProfileRequest request
    ) {

        UserProfile profile = userRepository.findByAuthUserId(userId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Профиль пользователя не найден"
                ));

        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setMiddleName(request.getMiddleName());
        profile.setPhone(request.getPhone());

        profile.setCity(request.getCity());
        profile.setStreet(request.getStreet());
        profile.setHouse(request.getHouse());
        profile.setApartment(request.getApartment());

        return map(userRepository.save(profile), "CLIENT");
    }

    /**
     * Entity -> Response
     */
    private UserProfileResponse map(UserProfile profile, String role) {

        return UserProfileResponse.builder()
                .id(profile.getId())
                .authUserId(profile.getAuthUserId())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .middleName(profile.getMiddleName())
                .phone(profile.getPhone())
                .city(profile.getCity())
                .street(profile.getStreet())
                .house(profile.getHouse())
                .apartment(profile.getApartment())
                .zoneId(profile.getZoneId())
                .role(role)
                .build();
    }
}