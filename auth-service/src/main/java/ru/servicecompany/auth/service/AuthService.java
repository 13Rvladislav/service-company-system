package ru.servicecompany.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.servicecompany.auth.common.exception.ApiException;
import ru.servicecompany.auth.dto.request.*;
import ru.servicecompany.auth.dto.response.AdminUserResponse;
import ru.servicecompany.auth.dto.response.CreateEmployeeResponse;
import ru.servicecompany.auth.dto.response.LoginResponse;
import ru.servicecompany.auth.dto.response.UserResponse;
import ru.servicecompany.auth.entity.Role;
import ru.servicecompany.auth.entity.RoleName;
import ru.servicecompany.auth.entity.User;
import ru.servicecompany.auth.kafka.UserProfileProducer;
import ru.servicecompany.auth.repository.RoleRepository;
import ru.servicecompany.auth.repository.UserRepository;
import ru.servicecompany.auth.security.JwtService;
import ru.servicecompany.auth.dto.response.AdminUserCardResponse;
import ru.servicecompany.auth.kafka.AdminProfileRequestProducer;
import ru.servicecompany.auth.kafka.ProfileRequestManager;
import ru.servicecompany.auth.kafka.event.ProfileResponseEvent;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;

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

    /**
     * Регистрация клиента.
     */
    public UserResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Пользователь с таким Email уже существует"
            );
        }

        if (userRepository.existsByPhone(request.getPhone())) {
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

        return map(user);
    }

    /**
     * Авторизация.
     */
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException(
                        HttpStatus.UNAUTHORIZED,
                        "Неверный Email или пароль"
                ));

        /*
         * Пользователь заблокирован администратором.
         *
         * Проверяем это до проверки пароля,
         * чтобы пользователь получил понятное сообщение.
         */
        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "Ваша учетная запись была заблокирована администратором"
            );
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {
            throw new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    "Неверный Email или пароль"
            );
        }

        String token = jwtService.generateToken(user);

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
     * <p>
     * Аккаунт создаётся в auth-service,
     * профиль создаётся асинхронно в user-service через Kafka.
     */
    public CreateEmployeeResponse createEmployee(
            CreateEmployeeRequest request
    ) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Пользователь с таким Email уже существует"
            );
        }

        if (userRepository.existsByPhone(request.getPhone())) {
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

        String temporaryPassword =
                generateTemporaryPassword();

        User user = new User();

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setMiddleName(request.getMiddleName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setPassword(
                passwordEncoder.encode(temporaryPassword)
        );
        user.setRole(role);

        /*
         * Создаём нового пользователя.
         */
        userRepository.save(user);

        try {

            /*
             * Отправляем событие создания профиля.
             */
            userProfileProducer.sendEmployeeProfileCreate(
                    user,
                    request
            );

        } catch (Exception e) {

            /*
             * Событие не удалось отправить.
             *
             * User был создан только этой операцией,
             * поэтому его можно удалить.
             */
            userRepository.deleteById(
                    user.getId()
            );

            throw e;
        }

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
            password.append(
                    chars.charAt(random.nextInt(chars.length()))
            );
        }

        return password.toString();
    }

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
     * <p>
     * Данные собираются из:
     * <p>
     * auth-service:
     * - email
     * - enabled
     * - основные данные User
     * <p>
     * user-service:
     * - профиль в зависимости от роли
     */
    public AdminUserCardResponse getUserCard(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Пользователь не найден"
                ));

        String role =
                user.getRole()
                        .getName()
                        .name();

        UUID requestId = UUID.randomUUID();

        CompletableFuture<ProfileResponseEvent> future =
                profileRequestManager.register(requestId);

        try {

            adminProfileRequestProducer.requestProfile(
                    requestId,
                    user.getId(),
                    role
            );

            ProfileResponseEvent profile =
                    future.get(
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
                    .specialization(profile.getSpecialization())
                    .status(profile.getStatus())

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

            profileRequestManager.remove(
                    requestId
            );
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

        user.setEnabled(enabled);

        userRepository.save(user);

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