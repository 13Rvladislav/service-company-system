package ru.servicecompany.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
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

import java.io.IOException;
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
    @Transactional(readOnly = true)
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
                        .hasAvatar(p.getAvatar() != null)
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
                        .hasAvatar(p.getAvatar() != null)
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
                        .hasAvatar(p.getAvatar() != null)
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
     * Загрузить или заменить фото профиля.
     */
    public void uploadAvatar(
            UUID userId,
            String role,
            MultipartFile file
    ) throws IOException {

        byte[] avatar = file.getBytes();

        switch (role) {

            case "CLIENT" -> {
                UserProfile profile = userRepository.findByAuthUserId(userId)
                        .orElseThrow(() -> new ApiException(
                                HttpStatus.NOT_FOUND,
                                "Профиль пользователя не найден"
                        ));

                profile.setAvatar(avatar);
                userRepository.save(profile);
            }

            case "ENGINEER" -> {
                MasterProfile profile = masterRepository.findByAuthUserId(userId)
                        .orElseThrow(() -> new ApiException(
                                HttpStatus.NOT_FOUND,
                                "Профиль мастера не найден"
                        ));

                profile.setAvatar(avatar);
                masterRepository.save(profile);
            }

            case "DISPATCHER" -> {
                DispatcherProfile profile = dispatcherRepository.findByAuthUserId(userId)
                        .orElseThrow(() -> new ApiException(
                                HttpStatus.NOT_FOUND,
                                "Профиль диспетчера не найден"
                        ));

                profile.setAvatar(avatar);
                dispatcherRepository.save(profile);
            }

            case "ADMIN" -> {
                AdminProfile profile = adminRepository.findByAuthUserId(userId)
                        .orElseThrow(() -> new ApiException(
                                HttpStatus.NOT_FOUND,
                                "Профиль администратора не найден"
                        ));

                profile.setAvatar(avatar);
                adminRepository.save(profile);
            }

            default -> throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Неизвестная роль"
            );
        }
    }

    /**
     * Получить фото профиля.
     */
    @Transactional(readOnly = true)
    public byte[] getAvatar(UUID userId, String role) {

        return switch (role) {

            case "CLIENT" -> userRepository.findByAuthUserId(userId)
                    .orElseThrow(() -> new ApiException(
                            HttpStatus.NOT_FOUND,
                            "Профиль пользователя не найден"
                    ))
                    .getAvatar();

            case "ENGINEER" -> masterRepository.findByAuthUserId(userId)
                    .orElseThrow(() -> new ApiException(
                            HttpStatus.NOT_FOUND,
                            "Профиль мастера не найден"
                    ))
                    .getAvatar();

            case "DISPATCHER" -> dispatcherRepository.findByAuthUserId(userId)
                    .orElseThrow(() -> new ApiException(
                            HttpStatus.NOT_FOUND,
                            "Профиль диспетчера не найден"
                    ))
                    .getAvatar();

            case "ADMIN" -> adminRepository.findByAuthUserId(userId)
                    .orElseThrow(() -> new ApiException(
                            HttpStatus.NOT_FOUND,
                            "Профиль администратора не найден"
                    ))
                    .getAvatar();

            default -> throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Неизвестная роль"
            );
        };
    }

    /**
     * Entity -> Response
     */
    private UserProfileResponse map(UserProfile profile, String role) {

        System.out.println("AVATAR = " + profile.getAvatar());

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
                .hasAvatar(profile.getAvatar() != null)
                .role(role)
                .build();
    }
}