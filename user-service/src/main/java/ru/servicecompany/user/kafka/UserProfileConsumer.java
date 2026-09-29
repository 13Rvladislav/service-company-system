package ru.servicecompany.user.kafka;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.servicecompany.user.dto.request.CreateAdminProfileRequest;
import ru.servicecompany.user.dto.request.CreateDispatcherProfileRequest;
import ru.servicecompany.user.dto.request.CreateMasterProfileRequest;
import ru.servicecompany.user.dto.request.CreateUserProfileRequest;
import ru.servicecompany.user.kafka.event.UserProfileCreateEvent;
import ru.servicecompany.user.service.InternalProfileService;
import ru.servicecompany.user.service.UserProfileService;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserProfileConsumer {

    private final UserProfileService userProfileService;
    private final InternalProfileService internalProfileService;
    private final ProfileResultProducer profileResultProducer;

    @KafkaListener(
            topics = KafkaTopics.USER_PROFILE_CREATE,
            groupId = "user-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(UserProfileCreateEvent event) {

        log.info("========================================");
        log.info("KAFKA MESSAGE RECEIVED");
        log.info("authUserId = {}", event.getAuthUserId());
        log.info("role      = {}", event.getRole());
        log.info("firstName = {}", event.getFirstName());
        log.info("lastName  = {}", event.getLastName());
        log.info("phone     = {}", event.getPhone());
        log.info("employeeNumber = {}", event.getEmployeeNumber());
        log.info("zoneId    = {}", event.getZoneId());
        log.info("========================================");

        if (event.getAuthUserId() == null) {
            throw new IllegalArgumentException(
                    "authUserId не может быть null"
            );
        }

        if (event.getRole() == null || event.getRole().isBlank()) {
            throw new IllegalArgumentException(
                    "role не может быть null или пустым"
            );
        }

        try {

            switch (event.getRole()) {

                case "CLIENT" -> createClient(event);

                case "ENGINEER" -> createEngineer(event);

                case "DISPATCHER" -> createDispatcher(event);

                case "ADMIN" -> createAdmin(event);

                default -> throw new IllegalArgumentException(
                        "Неизвестная роль: " + event.getRole()
                );
            }

            log.info("========================================");
            log.info("PROFILE CREATED SUCCESSFULLY");
            log.info("authUserId = {}", event.getAuthUserId());
            log.info("role      = {}", event.getRole());
            log.info("========================================");

        } catch (Exception e) {

            log.error(
                    "========================================"
            );

            log.error(
                    "PROFILE CREATION FAILED"
            );

            log.error(
                    "authUserId = {}",
                    event.getAuthUserId()
            );

            log.error(
                    "role = {}",
                    event.getRole()
            );

            log.error(
                    "reason = {}",
                    e.getMessage(),
                    e
            );

            log.error(
                    "========================================"
            );

            profileResultProducer.profileFailed(
                    event.getAuthUserId(),
                    e.getMessage() != null
                            ? e.getMessage()
                            : "Неизвестная ошибка создания профиля"
            );

            /*
             * ВАЖНО:
             *
             * Здесь не бросаем exception дальше.
             *
             * Иначе Kafka снова отправит сообщение
             * на retry, и rollback-событие будет отправляться
             * несколько раз.
             */
            return;
        }
    }

    private void createClient(
            UserProfileCreateEvent event
    ) {

        CreateUserProfileRequest request =
                new CreateUserProfileRequest();

        request.setAuthUserId(
                event.getAuthUserId()
        );

        request.setFirstName(
                event.getFirstName()
        );

        request.setLastName(
                event.getLastName()
        );

        request.setMiddleName(
                event.getMiddleName()
        );

        request.setPhone(
                event.getPhone()
        );

        log.info(
                "Создание CLIENT profile: authUserId={}",
                event.getAuthUserId()
        );

        userProfileService.create(request);
    }

    private void createEngineer(
            UserProfileCreateEvent event
    ) {

        CreateMasterProfileRequest request =
                new CreateMasterProfileRequest();

        request.setAuthUserId(
                event.getAuthUserId()
        );

        request.setFirstName(
                event.getFirstName()
        );

        request.setLastName(
                event.getLastName()
        );

        request.setMiddleName(
                event.getMiddleName()
        );

        request.setPhone(
                event.getPhone()
        );

        request.setEmployeeNumber(
                event.getEmployeeNumber()
        );

        request.setSpecialization(
                event.getSpecialization()
        );

        request.setZoneId(
                event.getZoneId()
        );

        log.info(
                "Создание ENGINEER profile: authUserId={}, zoneId={}",
                event.getAuthUserId(),
                event.getZoneId()
        );

        internalProfileService.createMaster(request);
    }

    private void createDispatcher(
            UserProfileCreateEvent event
    ) {

        CreateDispatcherProfileRequest request =
                new CreateDispatcherProfileRequest();

        request.setAuthUserId(
                event.getAuthUserId()
        );

        request.setFirstName(
                event.getFirstName()
        );

        request.setLastName(
                event.getLastName()
        );

        request.setMiddleName(
                event.getMiddleName()
        );

        request.setPhone(
                event.getPhone()
        );

        request.setEmployeeNumber(
                event.getEmployeeNumber()
        );

        request.setDepartment(
                event.getDepartment()
        );

        log.info(
                "Создание DISPATCHER profile: authUserId={}",
                event.getAuthUserId()
        );

        internalProfileService.createDispatcher(request);
    }

    private void createAdmin(
            UserProfileCreateEvent event
    ) {

        CreateAdminProfileRequest request =
                new CreateAdminProfileRequest();

        request.setAuthUserId(
                event.getAuthUserId()
        );

        request.setFirstName(
                event.getFirstName()
        );

        request.setLastName(
                event.getLastName()
        );

        request.setMiddleName(
                event.getMiddleName()
        );

        request.setPhone(
                event.getPhone()
        );

        request.setEmployeeNumber(
                event.getEmployeeNumber()
        );

        request.setPosition(
                event.getPosition()
        );

        log.info(
                "Создание ADMIN profile: authUserId={}",
                event.getAuthUserId()
        );

        internalProfileService.createAdmin(request);
    }

    @PostConstruct
    public void init() {

        log.info("========================================");
        log.info("USER PROFILE KAFKA CONSUMER INITIALIZED");
        log.info(
                "Topic: {}",
                KafkaTopics.USER_PROFILE_CREATE
        );
        log.info(
                "Group: user-service-group"
        );
        log.info("========================================");
    }
}