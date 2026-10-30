
package ru.servicecompany.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.servicecompany.auth.common.exception.ApiException;
import ru.servicecompany.auth.dto.request.*;
import ru.servicecompany.auth.dto.response.AdminUserCardResponse;
import ru.servicecompany.auth.dto.response.AdminUserResponse;
import ru.servicecompany.auth.dto.response.CreateEmployeeResponse;
import ru.servicecompany.auth.dto.response.LoginResponse;
import ru.servicecompany.auth.dto.response.UserResponse;
import ru.servicecompany.auth.entity.Role;
import ru.servicecompany.auth.entity.RoleName;
import ru.servicecompany.auth.entity.User;
import ru.servicecompany.auth.kafka.*;
import ru.servicecompany.auth.kafka.event.ProfileDeleteResponseEvent;
import ru.servicecompany.auth.kafka.event.ProfileResponseEvent;
import ru.servicecompany.auth.kafka.event.ProfileUpdateResponseEvent;
import ru.servicecompany.auth.repository.RoleRepository;
import ru.servicecompany.auth.repository.UserRepository;
import ru.servicecompany.auth.security.JwtService;

import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserProfileProducer userProfileProducer;
    private final AdminProfileRequestProducer adminProfileRequestProducer;
    private final ProfileRequestManager profileRequestManager;
    private final ProfileUpdateRequestProducer profileUpdateRequestProducer;
    private final ProfileUpdateRequestManager profileUpdateRequestManager;
    private final ProfileDeleteRequestProducer profileDeleteRequestProducer;
    private final ProfileDeleteRequestManager profileDeleteRequestManager;

    /**
     * Регистрация клиента.
     */
    public UserResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn(
                    "BUSINESS_EVENT event=USER_REGISTRATION_FAILED reason=EMAIL_ALREADY_EXISTS"
            );

            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Пользователь с таким Email уже существует"
            );
        }

        if (userRepository.existsByPhone(request.getPhone())) {
            log.warn(
                    "BUSINESS_EVENT event=USER_REGISTRATION_FAILED reason=PHONE_ALREADY_EXISTS"
            );

            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Пользователь с таким телефоном уже существует"
            );
        }

        Role role = roleRepository.findByName(RoleName.CLIENT)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Роль CLIENT не найдена"
                ));

        User user = new User();

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setMiddleName(request.getMiddleName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);

        userRepository.save(user);
        userProfileProducer.sendClientProfileCreate(user);

        log.info(
                "BUSINESS_EVENT event=USER_REGISTERED userId={} role={}",
                user.getId(),
                role.getName()
        );

        return map(user);
    }

    /**
     * Авторизация.
     */
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn(
                            "BUSINESS_EVENT event=LOGIN_FAILED reason=USER_NOT_FOUND"
                    );

                    return new ApiException(
                            HttpStatus.UNAUTHORIZED,
                            "Неверный Email или пароль"
                    );
                });

        if (!Boolean.TRUE.equals(user.getEnabled())) {
            log.warn(
                    "BUSINESS_EVENT event=LOGIN_FAILED userId={} reason=ACCOUNT_DISABLED",
                    user.getId()
            );

            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "Ваша учетная запись была заблокирована администратором"
            );
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {
            log.warn(
                    "BUSINESS_EVENT event=LOGIN_FAILED userId={} reason=INVALID_CREDENTIALS",
                    user.getId()
            );

            throw new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    "Неверный Email или пароль"
            );
        }

        String token = jwtService.generateToken(user);

        log.info(
                "BUSINESS_EVENT event=LOGIN_SUCCEEDED userId={} role={}",
                user.getId(),
                user.getRole().getName()
        );

        return new LoginResponse(token);
    }

    /**
     * Текущий пользователь.
     */
    public UserResponse getCurrentUser(User user) {
        return map(user);
    }

    /**
     * Создание сотрудника.
     */
    public CreateEmployeeResponse createEmployee(
            CreateEmployeeRequest request
    ) {

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn(
                    "BUSINESS_EVENT event=EMPLOYEE_CREATION_FAILED reason=EMAIL_ALREADY_EXISTS"
            );

            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Пользователь с таким Email уже существует"
            );
        }

        if (userRepository.existsByPhone(request.getPhone())) {
            log.warn(
                    "BUSINESS_EVENT event=EMPLOYEE_CREATION_FAILED reason=PHONE_ALREADY_EXISTS"
            );

            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Пользователь с таким телефоном уже существует"
            );
        }

        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Роль не найдена"
                ));

        String temporaryPassword = generateTemporaryPassword();

        User user = new User();

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setMiddleName(request.getMiddleName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(temporaryPassword));
        user.setRole(role);

        userRepository.save(user);

        try {
            userProfileProducer.sendEmployeeProfileCreate(user, request);
        } catch (Exception e) {
            userRepository.deleteById(user.getId());

            log.error(
                    "BUSINESS_EVENT event=EMPLOYEE_CREATION_FAILED userId={} role={} reason=PROFILE_CREATION_ERROR",
                    user.getId(),
                    role.getName()
            );

            throw e;
        }

        log.info(
                "BUSINESS_EVENT event=EMPLOYEE_CREATED userId={} role={}",
                user.getId(),
                role.getName()
        );

        return new CreateEmployeeResponse(
                user.getEmail(),
                temporaryPassword,
                "Сотрудник успешно создан"
        );
    }

    /**
     * Генерация временного пароля.
     */
    private String generateTemporaryPassword() {

        String chars =
                "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < 10; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }

    /**
     * Получение пользователей по роли.
     */
    public List<AdminUserResponse> getUsersByRole(RoleName roleName) {

        return userRepository.findAllByRole_Name(roleName)
                .stream()
                .map(user -> AdminUserResponse.builder()
                        .id(user.getId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .middleName(user.getMiddleName())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .role(user.getRole().getName().name())
                        .enabled(user.getEnabled())
                        .build())
                .toList();
    }

    /**
     * Получение полной карточки пользователя.
     * Данные профиля запрашиваются из user-service через Kafka.
     */
    public AdminUserCardResponse getUserCard(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Пользователь не найден"
                ));

        String role = user.getRole().getName().name();
        UUID requestId = UUID.randomUUID();

        CompletableFuture<ProfileResponseEvent> future =
                profileRequestManager.register(requestId);

        try {
            adminProfileRequestProducer.requestProfile(
                    requestId,
                    user.getId(),
                    role
            );

            ProfileResponseEvent profile = future.get(
                    5,
                    TimeUnit.SECONDS
            );

            return AdminUserCardResponse.builder()
                    .id(user.getId())
                    .authUserId(profile.getAuthUserId())

                    .firstName(profile.getFirstName())
                    .lastName(profile.getLastName())
                    .middleName(profile.getMiddleName())

                    .email(user.getEmail())
                    .phone(profile.getPhone())
                    .role(role)

                    .enabled(user.getEnabled())
                    .hasAvatar(profile.getHasAvatar())

                    .cityId(profile.getCityId())
                    .streetId(profile.getStreetId())
                    .houseId(profile.getHouseId())
                    .apartment(profile.getApartment())

                    .employeeNumber(profile.getEmployeeNumber())
                    .specializations(profile.getSpecializations())
                    .status(profile.getStatus())

                    .zones(profile.getZones())

                    .department(profile.getDepartment())
                    .position(profile.getPosition())
                    .build();

        } catch (TimeoutException e) {
            throw new ApiException(
                    HttpStatus.GATEWAY_TIMEOUT,
                    "Не удалось получить профиль пользователя"
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Получение профиля было прервано"
            );

        } catch (Exception e) {
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Ошибка получения профиля пользователя"
            );

        } finally {
            profileRequestManager.remove(requestId);
        }
    }

    /**
     * Блокировка или разблокировка пользователя.
     */
    public AdminUserResponse setUserEnabled(
            UUID userId,
            boolean enabled
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Пользователь не найден"
                ));

        boolean previousEnabled = Boolean.TRUE.equals(user.getEnabled());

        user.setEnabled(enabled);
        userRepository.save(user);

        log.info(
                "BUSINESS_EVENT event=USER_ACCOUNT_STATUS_CHANGED userId={} previousEnabled={} newEnabled={}",
                user.getId(),
                previousEnabled,
                enabled
        );

        return AdminUserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .middleName(user.getMiddleName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().getName().name())
                .enabled(user.getEnabled())
                .build();
    }

    /**
     * Обновление пользователя и его профиля.
     */
    public AdminUserCardResponse updateUser(
            UUID userId,
            UpdateAdminUserRequest request
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Пользователь не найден"
                ));

        if (!user.getEmail().equalsIgnoreCase(request.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Пользователь с таким Email уже существует"
            );
        }

        if (!user.getPhone().equals(request.getPhone())
                && userRepository.existsByPhone(request.getPhone())) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Пользователь с таким телефоном уже существует"
            );
        }

        String role = user.getRole().getName().name();
        UUID requestId = UUID.randomUUID();

        CompletableFuture<ProfileUpdateResponseEvent> future =
                profileUpdateRequestManager.register(requestId);

        try {
            profileUpdateRequestProducer.updateProfile(
                    requestId,
                    user.getId(),
                    role,
                    request.getFirstName(),
                    request.getLastName(),
                    request.getMiddleName(),
                    request.getPhone(),

                    request.getHouseId(),
                    request.getApartment(),

                    request.getEmployeeNumber(),
                    request.getSpecialization(),
                    request.getZoneId(),
                    request.getStatus(),

                    request.getDepartment(),

                    request.getPosition()
            );

            ProfileUpdateResponseEvent response = future.get(
                    5,
                    TimeUnit.SECONDS
            );

            if (!response.isSuccess()) {
                throw new ApiException(
                        HttpStatus.BAD_REQUEST,
                        response.getReason() != null
                                ? response.getReason()
                                : "Не удалось обновить профиль пользователя"
                );
            }

            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setMiddleName(request.getMiddleName());
            user.setPhone(request.getPhone());
            user.setEmail(request.getEmail());

            userRepository.save(user);

            log.info(
                    "BUSINESS_EVENT event=USER_UPDATED userId={} role={}",
                    user.getId(),
                    role
            );

            return getUserCard(userId);

        } catch (TimeoutException e) {
            throw new ApiException(
                    HttpStatus.GATEWAY_TIMEOUT,
                    "Не удалось обновить профиль пользователя"
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Обновление пользователя было прервано"
            );

        } catch (ApiException e) {
            throw e;

        } catch (Exception e) {
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Ошибка обновления пользователя"
            );

        } finally {
            profileUpdateRequestManager.remove(requestId);
        }
    }

    /**
     * Удаление пользователя и его профиля.
     */
    public void deleteUser(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Пользователь не найден"
                ));

        String role = user.getRole().getName().name();
        UUID requestId = UUID.randomUUID();

        CompletableFuture<ProfileDeleteResponseEvent> future =
                profileDeleteRequestManager.register(requestId);

        try {
            profileDeleteRequestProducer.deleteProfile(
                    requestId,
                    user.getId(),
                    role
            );

            ProfileDeleteResponseEvent response = future.get(
                    5,
                    TimeUnit.SECONDS
            );

            if (!response.isSuccess()) {
                throw new ApiException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        response.getReason() != null
                                ? response.getReason()
                                : "Не удалось удалить профиль пользователя"
                );
            }

            userRepository.delete(user);

            log.info(
                    "BUSINESS_EVENT event=USER_DELETED userId={} role={}",
                    userId,
                    role
            );

        } catch (TimeoutException e) {
            throw new ApiException(
                    HttpStatus.GATEWAY_TIMEOUT,
                    "Не удалось удалить профиль пользователя"
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Удаление пользователя было прервано"
            );

        } catch (ApiException e) {
            throw e;

        } catch (Exception e) {
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Ошибка удаления пользователя"
            );

        } finally {
            profileDeleteRequestManager.remove(requestId);
        }
    }

    /**
     * Entity -> DTO.
     */
    private UserResponse map(User user) {

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .middleName(user.getMiddleName())
                .phone(user.getPhone())
                .role(user.getRole().getName().name())
                .build();
    }
}
