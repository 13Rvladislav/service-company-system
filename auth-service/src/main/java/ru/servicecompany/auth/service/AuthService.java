package ru.servicecompany.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.servicecompany.auth.common.exception.ApiException;
import ru.servicecompany.auth.dto.request.CreateUserProfileRequest;
import ru.servicecompany.auth.dto.request.LoginRequest;
import ru.servicecompany.auth.dto.request.RegisterRequest;
import ru.servicecompany.auth.dto.response.LoginResponse;
import ru.servicecompany.auth.dto.response.UserResponse;
import ru.servicecompany.auth.entity.Role;
import ru.servicecompany.auth.entity.RoleName;
import ru.servicecompany.auth.entity.User;
import ru.servicecompany.auth.repository.RoleRepository;
import ru.servicecompany.auth.repository.UserRepository;
import ru.servicecompany.auth.security.JwtService;

@Service
@RequiredArgsConstructor
public class AuthService {

    // Работа с пользователями
    private final UserRepository userRepository;

    // Работа с ролями
    private final RoleRepository roleRepository;

    // BCrypt (хеширование и проверка паролей)
    private final PasswordEncoder passwordEncoder;
    // Клиент второго микросервиса
    private final UserServiceClient userServiceClient;
    // Генерация JWT токенов
    private final JwtService jwtService;

    /**
     * Регистрация нового клиента.
     */
    /**
     * Регистрация нового пользователя.
     * 1. Проверяем уникальность email и телефона.
     * 2. Создаём пользователя в auth_db.
     * 3. Создаём профиль в user-service.
     */
    @Transactional
    public UserResponse register(RegisterRequest request) {

        // Проверка email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(HttpStatus.CONFLICT, "Email уже используется");
        }

        // Проверка телефона
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new ApiException(HttpStatus.CONFLICT, "Телефон уже используется");
        }

        // Получаем роль CLIENT
        Role clientRole = roleRepository.findByName(RoleName.CLIENT)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Роль CLIENT не найдена"));

        // Создаём пользователя
        User user = new User();

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setMiddleName(request.getMiddleName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());


        // Храним только хэш пароля
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(clientRole);

        // Сохраняем в auth_db
        User saved = userRepository.save(user);

        // Создаём профиль в user-service
        userServiceClient.createProfile(
                CreateUserProfileRequest.builder()
                        .authUserId(saved.getId())
                        .firstName(saved.getFirstName())
                        .lastName(saved.getLastName())
                        .middleName(saved.getMiddleName())
                        .phone(saved.getPhone())
                        .build()
        );

        // Ответ клиенту
        return UserResponse.builder()
                .id(saved.getId())
                .firstName(saved.getFirstName())
                .lastName(saved.getLastName())
                .middleName(saved.getMiddleName())
                .email(saved.getEmail())
                .phone(saved.getPhone())
                .role(saved.getRole().getName().name())
                .build();
    }

    /**
     * Авторизация пользователя.
     */
    public LoginResponse login(LoginRequest request) {

        // Ищем пользователя по Email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException(
                        HttpStatus.UNAUTHORIZED,
                        "Неверный email или пароль"
                ));

        /*
         * Сравниваем введённый пароль с BCrypt-хешем из базы.
         * Если хотя бы один символ отличается — вернётся false.
         */
        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {
            throw new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    "Неверный email или пароль"
            );
        }

        // Генерируем JWT
        String token = jwtService.generateToken(user);

        // Возвращаем токен клиенту
        return new LoginResponse(token);
    }

    /**
     * Возвращает данные текущего авторизованного пользователя.
     */
    public UserResponse getCurrentUser(User user) {

        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .middleName(user.getMiddleName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().getName().name())
                .build();
    }
}