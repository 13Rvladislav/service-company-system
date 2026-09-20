package ru.servicecompany.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.servicecompany.auth.common.exception.ApiException;
import ru.servicecompany.auth.dto.request.ForgotPasswordRequest;
import ru.servicecompany.auth.dto.request.ResetPasswordRequest;
import ru.servicecompany.auth.dto.response.MessageResponse;
import ru.servicecompany.auth.entity.PasswordResetToken;
import ru.servicecompany.auth.entity.User;
import ru.servicecompany.auth.repository.PasswordResetTokenRepository;
import ru.servicecompany.auth.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ForgotPasswordService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    /**
     * Создание токена восстановления.
     */
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);

        // Не раскрываем существует ли email
        if (user == null) {
            return new MessageResponse(
                    "Если аккаунт существует, инструкция отправлена на почту"
            );
        }

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .build();

        tokenRepository.save(resetToken);

        emailService.sendResetPasswordEmail(user.getEmail(), token);

        return new MessageResponse(
                "Если аккаунт существует, инструкция отправлена на почту"
        );
    }

    /**
     * Установка нового пароля.
     */
    public MessageResponse resetPassword(ResetPasswordRequest request) {

        PasswordResetToken resetToken = tokenRepository
                .findByToken(request.getToken())
                .orElseThrow(() ->
                        new ApiException(
                                HttpStatus.BAD_REQUEST,
                                "Недействительный токен"
                        )
                );

        if (resetToken.getUsed()) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Токен уже использован"
            );
        }

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Срок действия токена истёк"
            );
        }

        User user = resetToken.getUser();

        user.setPassword(
                passwordEncoder.encode(request.getNewPassword())
        );

        userRepository.save(user);

        resetToken.setUsed(true);

        tokenRepository.save(resetToken);

        return new MessageResponse("Пароль успешно изменён");
    }
}