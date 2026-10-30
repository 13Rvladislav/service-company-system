package ru.servicecompany.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.servicecompany.auth.common.exception.ApiException;
import ru.servicecompany.auth.dto.request.ForgotPasswordRequest;
import ru.servicecompany.auth.dto.request.ResetPasswordRequest;
import ru.servicecompany.auth.dto.response.MessageResponse;
import ru.servicecompany.auth.entity.PasswordResetToken;
import ru.servicecompany.auth.entity.User;
import ru.servicecompany.auth.enums.AuditResult;
import ru.servicecompany.auth.repository.PasswordResetTokenRepository;
import ru.servicecompany.auth.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ForgotPasswordService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AuditService auditService;

    /**
     * Создание токена восстановления.
     */
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);

        // Не раскрываем, существует ли email.
        if (user == null) {

            log.info(
                    "BUSINESS_EVENT event=PASSWORD_RESET_REQUEST_IGNORED reason=USER_NOT_FOUND"
            );

            auditService.logAuthEvent(
                    "PASSWORD_RESET_REQUEST",
                    null,
                    "ANONYMOUS",
                    AuditResult.SUCCESS,
                    "result=GENERIC_RESPONSE"
            );

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

        log.info(
                "BUSINESS_EVENT event=PASSWORD_RESET_REQUESTED userId={}",
                user.getId()
        );

        auditService.logAuthEvent(
                "PASSWORD_RESET_REQUEST",
                user.getId(),
                user.getRole().getName().name(),
                AuditResult.SUCCESS,
                "result=EMAIL_SENT"
        );

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
                .orElseThrow(() -> {

                    log.warn(
                            "BUSINESS_EVENT event=PASSWORD_RESET_FAILED reason=INVALID_TOKEN"
                    );

                    auditService.logAuthEvent(
                            "PASSWORD_RESET_FAILURE",
                            null,
                            "ANONYMOUS",
                            AuditResult.FAILURE,
                            "reason=INVALID_TOKEN"
                    );

                    return new ApiException(
                            HttpStatus.BAD_REQUEST,
                            "Недействительный токен"
                    );
                });

        if (resetToken.getUsed()) {

            log.warn(
                    "BUSINESS_EVENT event=PASSWORD_RESET_FAILED userId={} reason=TOKEN_ALREADY_USED",
                    resetToken.getUser().getId()
            );

            auditService.logAuthEvent(
                    "PASSWORD_RESET_FAILURE",
                    resetToken.getUser().getId(),
                    resetToken.getUser().getRole().getName().name(),
                    AuditResult.FAILURE,
                    "reason=TOKEN_ALREADY_USED"
            );

            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Токен уже использован"
            );
        }

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {

            log.warn(
                    "BUSINESS_EVENT event=PASSWORD_RESET_FAILED userId={} reason=TOKEN_EXPIRED",
                    resetToken.getUser().getId()
            );

            auditService.logAuthEvent(
                    "PASSWORD_RESET_FAILURE",
                    resetToken.getUser().getId(),
                    resetToken.getUser().getRole().getName().name(),
                    AuditResult.FAILURE,
                    "reason=TOKEN_EXPIRED"
            );

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

        log.info(
                "BUSINESS_EVENT event=PASSWORD_RESET_SUCCEEDED userId={}",
                user.getId()
        );

        auditService.logAuthEvent(
                "PASSWORD_RESET_SUCCESS",
                user.getId(),
                user.getRole().getName().name(),
                AuditResult.SUCCESS,
                "password=CHANGED"
        );

        return new MessageResponse("Пароль успешно изменён");
    }
}