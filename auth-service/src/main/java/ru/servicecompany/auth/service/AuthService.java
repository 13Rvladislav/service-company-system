package ru.servicecompany.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.servicecompany.auth.dto.request.RegisterRequest;
import ru.servicecompany.auth.dto.response.UserResponse;
import ru.servicecompany.auth.entity.Role;
import ru.servicecompany.auth.entity.RoleName;
import ru.servicecompany.auth.entity.User;
import ru.servicecompany.auth.repository.RoleRepository;
import ru.servicecompany.auth.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email уже используется");
        }

        if (userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Телефон уже используется");
        }

        Role clientRole = roleRepository.findByName(RoleName.CLIENT)
                .orElseThrow();

        User user = new User();

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(clientRole);

        User saved = userRepository.save(user);

        return UserResponse.builder()
                .id(saved.getId())
                .firstName(saved.getFirstName())
                .lastName(saved.getLastName())
                .email(saved.getEmail())
                .phone(saved.getPhone())
                .role(saved.getRole().getName().name())
                .build();
    }

}