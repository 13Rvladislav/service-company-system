
package ru.servicecompany.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * Отправка письма для восстановления пароля.
     */
    public void sendResetPasswordEmail(String email, String token) {

        String link = "http://localhost:63342/service-company-system/web/reset.html?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("vlad.rozbash@yandex.ru");
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

        try {
            mailSender.send(message);

            log.info(
                    "BUSINESS_EVENT event=PASSWORD_RESET_EMAIL_SENT"
            );

        } catch (MailException e) {
            log.error(
                    "BUSINESS_EVENT event=PASSWORD_RESET_EMAIL_FAILED exceptionType={}",
                    e.getClass().getSimpleName()
            );

            throw e;
        }
    }
}
