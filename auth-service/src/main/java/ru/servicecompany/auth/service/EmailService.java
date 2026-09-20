package ru.servicecompany.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * Отправка письма для восстановления пароля.
     */
    public void sendResetPasswordEmail(String email, String token) {

        String link = "http://localhost:3000/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("vlad.rozbash@yandex.ru");
        message.setTo(email);
        message.setSubject("Восстановление пароля");
        message.setTo(email);
        message.setSubject("Восстановление пароля");
        message.setText("""
                Здравствуйте!
                
                Вы запросили восстановление пароля.
                
                Перейдите по ссылке:
                
                %s
                
                Ссылка действует 15 минут.
                
                Если это были не вы — просто проигнорируйте письмо.
                """.formatted(link));

        mailSender.send(message);
    }
}