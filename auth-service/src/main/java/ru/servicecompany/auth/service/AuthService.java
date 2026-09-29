package ru.servicecompany.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.servicecompany.auth.common.exception.ApiException;
import ru.servicecompany.auth.dto.request.*;
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

import java.security.SecureRandom;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserProfileProducer userProfileProducer;

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
     *
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