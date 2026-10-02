package ru.servicecompany.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.servicecompany.user.common.exception.ApiException;
import ru.servicecompany.user.config.JwtUserPrincipal;
import ru.servicecompany.user.dto.request.CreateUserProfileRequest;
import ru.servicecompany.user.dto.request.UpdateUserProfileRequest;
import ru.servicecompany.user.dto.response.UserProfileResponse;
import ru.servicecompany.user.entity.*;
import ru.servicecompany.user.repository.*;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userRepository;
    private final MasterProfileRepository masterRepository;
    private final DispatcherProfileRepository dispatcherRepository;
    private final AdminProfileRepository adminRepository;
    private final HouseRepository houseRepository;

    /**
     * Создание профиля клиента.
     */
    public UserProfileResponse create(CreateUserProfileRequest request) {

        if (userRepository.existsByAuthUserId(request.getAuthUserId())) {
            throw new ApiException(HttpStatus.CONFLICT, "Профиль уже существует");
        }

        UserProfile profile = UserProfile.builder()
                .authUserId(request.getAuthUserId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .middleName(request.getMiddleName())
                .phone(request.getPhone())
                .house(null)
                .apartment(null)
                .build();

        return map(userRepository.save(profile), "CLIENT", null);
    }

    /**
     * Получить профиль текущего пользователя.
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentProfile(JwtUserPrincipal principal) {

        return switch (principal.role()) {

            case "CLIENT" -> {
                UserProfile p = userRepository.findByAuthUserId(principal.userId())
                        .orElseThrow(() -> new ApiException(
                                HttpStatus.NOT_FOUND,
                                "Профиль пользователя не найден"
                        ));

                yield map(p, principal.role(), principal.email());
            }

            case "ENGINEER" -> {
                MasterProfile p = masterRepository.findByAuthUserId(principal.userId())
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
                        .email(principal.email())
                        .phone(p.getPhone())
                        .employeeNumber(p.getEmployeeNumber())
                        .specialization(p.getSpecialization())
                        .status(p.getStatus().name())
                        .role(principal.role())
                        .hasAvatar(p.getAvatar() != null && p.getAvatar().length > 0)
                        .build();
            }

            case "DISPATCHER" -> {
                DispatcherProfile p = dispatcherRepository.findByAuthUserId(principal.userId())
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
                        .email(principal.email())
                        .phone(p.getPhone())
                        .employeeNumber(p.getEmployeeNumber())
                        .department(p.getDepartment())
                        .role(principal.role())
                        .hasAvatar(p.getAvatar() != null && p.getAvatar().length > 0)
                        .build();
            }

            case "ADMIN" -> {
                AdminProfile p = adminRepository.findByAuthUserId(principal.userId())
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
                        .email(principal.email())
                        .phone(p.getPhone())
                        .employeeNumber(p.getEmployeeNumber())
                        .position(p.getPosition())
                        .role(principal.role())
                        .hasAvatar(p.getAvatar() != null && p.getAvatar().length > 0)
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
    @Transactional
    public UserProfileResponse updateCurrentProfile(
            JwtUserPrincipal principal,
            UpdateUserProfileRequest request
    ) {

        return switch (principal.role()) {

            case "CLIENT" -> {

                UserProfile profile = userRepository.findByAuthUserId(
                        principal.userId()
                ).orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Профиль пользователя не найден"
                ));

                if (request.getHouseId() == null) {
                    throw new ApiException(
                            HttpStatus.BAD_REQUEST,
                            "Необходимо выбрать адрес проживания"
                    );
                }

                House house = houseRepository.findById(request.getHouseId())
                        .orElseThrow(() -> new ApiException(
                                HttpStatus.NOT_FOUND,
                                "Дом не найден"
                        ));

                profile.setFirstName(request.getFirstName());
                profile.setLastName(request.getLastName());
                profile.setMiddleName(request.getMiddleName());
                profile.setPhone(request.getPhone());
                profile.setHouse(house);
                profile.setApartment(request.getApartment());

                userRepository.save(profile);

                yield map(profile, principal.role(), principal.email());
            }

            case "ENGINEER" -> {

                MasterProfile profile = masterRepository.findByAuthUserId(
                        principal.userId()
                ).orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Профиль мастера не найден"
                ));

                profile.setFirstName(request.getFirstName());
                profile.setLastName(request.getLastName());
                profile.setMiddleName(request.getMiddleName());
                profile.setPhone(request.getPhone());

                masterRepository.save(profile);

                yield UserProfileResponse.builder()
                        .id(profile.getId())
                        .authUserId(profile.getAuthUserId())
                        .firstName(profile.getFirstName())
                        .lastName(profile.getLastName())
                        .middleName(profile.getMiddleName())
                        .email(principal.email())
                        .phone(profile.getPhone())
                        .employeeNumber(profile.getEmployeeNumber())
                        .specialization(profile.getSpecialization())
                        .status(profile.getStatus().name())
                        .role(principal.role())
                        .hasAvatar(profile.getAvatar() != null && profile.getAvatar().length > 0)
                        .build();
            }

            case "DISPATCHER" -> {

                DispatcherProfile profile = dispatcherRepository.findByAuthUserId(
                        principal.userId()
                ).orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Профиль диспетчера не найден"
                ));

                profile.setFirstName(request.getFirstName());
                profile.setLastName(request.getLastName());
                profile.setMiddleName(request.getMiddleName());
                profile.setPhone(request.getPhone());

                dispatcherRepository.save(profile);

                yield UserProfileResponse.builder()
                        .id(profile.getId())
                        .authUserId(profile.getAuthUserId())
                        .firstName(profile.getFirstName())
                        .lastName(profile.getLastName())
                        .middleName(profile.getMiddleName())
                        .email(principal.email())
                        .phone(profile.getPhone())
                        .employeeNumber(profile.getEmployeeNumber())
                        .department(profile.getDepartment())
                        .role(principal.role())
                        .hasAvatar(profile.getAvatar() != null && profile.getAvatar().length > 0)
                        .build();
            }

            case "ADMIN" -> {

                AdminProfile profile = adminRepository.findByAuthUserId(
                        principal.userId()
                ).orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Профиль администратора не найден"
                ));

                profile.setFirstName(request.getFirstName());
                profile.setLastName(request.getLastName());
                profile.setMiddleName(request.getMiddleName());
                profile.setPhone(request.getPhone());

                adminRepository.save(profile);

                yield UserProfileResponse.builder()
                        .id(profile.getId())
                        .authUserId(profile.getAuthUserId())
                        .firstName(profile.getFirstName())
                        .lastName(profile.getLastName())
                        .middleName(profile.getMiddleName())
                        .email(principal.email())
                        .phone(profile.getPhone())
                        .employeeNumber(profile.getEmployeeNumber())
                        .position(profile.getPosition())
                        .role(principal.role())
                        .hasAvatar(profile.getAvatar() != null && profile.getAvatar().length > 0)
                        .build();
            }

            default -> throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Неизвестная роль"
            );
        };
    }

    /**
     * Загрузка аватара.
     */
    @Transactional
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
     * Получить аватар.
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

    @Transactional(readOnly = true)
    public UserProfileResponse getProfileByAuthUserId(
            UUID authUserId,
            String role
    ) {

        return switch (role) {

            case "CLIENT" -> {

                UserProfile profile = userRepository.findByAuthUserId(authUserId)
                        .orElseThrow(() -> new ApiException(
                                HttpStatus.NOT_FOUND,
                                "Профиль пользователя не найден"
                        ));

                yield map(profile, role, null);
            }

            case "ENGINEER" -> {

                MasterProfile profile = masterRepository.findByAuthUserId(authUserId)
                        .orElseThrow(() -> new ApiException(
                                HttpStatus.NOT_FOUND,
                                "Профиль мастера не найден"
                        ));

                yield UserProfileResponse.builder()
                        .id(profile.getId())
                        .authUserId(profile.getAuthUserId())
                        .firstName(profile.getFirstName())
                        .lastName(profile.getLastName())
                        .middleName(profile.getMiddleName())
                        .phone(profile.getPhone())
                        .employeeNumber(profile.getEmployeeNumber())
                        .specialization(profile.getSpecialization())
                        .status(profile.getStatus().name())
                        .role(role)
                        .hasAvatar(
                                profile.getAvatar() != null &&
                                        profile.getAvatar().length > 0
                        )
                        .build();
            }

            case "DISPATCHER" -> {

                DispatcherProfile profile =
                        dispatcherRepository.findByAuthUserId(authUserId)
                                .orElseThrow(() -> new ApiException(
                                        HttpStatus.NOT_FOUND,
                                        "Профиль диспетчера не найден"
                                ));

                yield UserProfileResponse.builder()
                        .id(profile.getId())
                        .authUserId(profile.getAuthUserId())
                        .firstName(profile.getFirstName())
                        .lastName(profile.getLastName())
                        .middleName(profile.getMiddleName())
                        .phone(profile.getPhone())
                        .employeeNumber(profile.getEmployeeNumber())
                        .department(profile.getDepartment())
                        .role(role)
                        .hasAvatar(
                                profile.getAvatar() != null &&
                                        profile.getAvatar().length > 0
                        )
                        .build();
            }

            case "ADMIN" -> {

                AdminProfile profile =
                        adminRepository.findByAuthUserId(authUserId)
                                .orElseThrow(() -> new ApiException(
                                        HttpStatus.NOT_FOUND,
                                        "Профиль администратора не найден"
                                ));

                yield UserProfileResponse.builder()
                        .id(profile.getId())
                        .authUserId(profile.getAuthUserId())
                        .firstName(profile.getFirstName())
                        .lastName(profile.getLastName())
                        .middleName(profile.getMiddleName())
                        .phone(profile.getPhone())
                        .employeeNumber(profile.getEmployeeNumber())
                        .position(profile.getPosition())
                        .role(role)
                        .hasAvatar(
                                profile.getAvatar() != null &&
                                        profile.getAvatar().length > 0
                        )
                        .build();
            }

            default -> throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Неизвестная роль"
            );
        };
    }

    @Transactional
    public void deleteProfile(
            UUID authUserId,
            String role
    ) {

        switch (role) {

            case "CLIENT" -> {

                UserProfile profile =
                        userRepository.findByAuthUserId(authUserId)
                                .orElseThrow(() -> new ApiException(
                                        HttpStatus.NOT_FOUND,
                                        "Профиль клиента не найден"
                                ));

                userRepository.delete(profile);
            }

            case "ENGINEER" -> {

                MasterProfile profile =
                        masterRepository.findByAuthUserId(authUserId)
                                .orElseThrow(() -> new ApiException(
                                        HttpStatus.NOT_FOUND,
                                        "Профиль мастера не найден"
                                ));

                masterRepository.delete(profile);
            }

            case "DISPATCHER" -> {

                DispatcherProfile profile =
                        dispatcherRepository.findByAuthUserId(authUserId)
                                .orElseThrow(() -> new ApiException(
                                        HttpStatus.NOT_FOUND,
                                        "Профиль диспетчера не найден"
                                ));

                dispatcherRepository.delete(profile);
            }

            case "ADMIN" -> {

                AdminProfile profile =
                        adminRepository.findByAuthUserId(authUserId)
                                .orElseThrow(() -> new ApiException(
                                        HttpStatus.NOT_FOUND,
                                        "Профиль администратора не найден"
                                ));

                adminRepository.delete(profile);
            }

            default -> throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Неизвестная роль"
            );
        }
    }

    /**
     * Entity -> Response
     */
    private UserProfileResponse map(
            UserProfile profile,
            String role,
            String email
    ) {

        UUID cityId = null;
        UUID streetId = null;
        UUID houseId = null;

        if (profile.getHouse() != null) {

            houseId = profile.getHouse().getId();

            if (profile.getHouse().getStreet() != null) {

                streetId = profile.getHouse().getStreet().getId();

                if (profile.getHouse().getStreet().getCity() != null) {
                    cityId = profile.getHouse().getStreet().getCity().getId();
                }
            }
        }

        boolean hasAvatar =
                profile.getAvatar() != null &&
                        profile.getAvatar().length > 0;

        return UserProfileResponse.builder()
                .id(profile.getId())
                .authUserId(profile.getAuthUserId())

                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .middleName(profile.getMiddleName())
                .email(email)
                .phone(profile.getPhone())

                .cityId(cityId)
                .streetId(streetId)
                .houseId(houseId)
                .apartment(profile.getApartment())

                .role(role)
                .hasAvatar(hasAvatar)
                .build();
    }
}