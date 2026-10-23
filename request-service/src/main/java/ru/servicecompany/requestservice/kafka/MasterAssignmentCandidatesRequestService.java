package ru.servicecompany.requestservice.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.servicecompany.requestservice.common.exception.ApiException;
import ru.servicecompany.requestservice.kafka.event.MasterAssignmentCandidatesResponseEvent;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
public class MasterAssignmentCandidatesRequestService {

    private final MasterAssignmentCandidatesRequestProducer producer;

    private final MasterAssignmentCandidatesResponseConsumer consumer;

    /**
     * Получение мастеров-кандидатов.
     *
     * zoneId != null:
     * автоматическое назначение — зона + специализация.
     *
     * zoneId == null:
     * ручное назначение — только специализация.
     */
    public List<UUID> getCandidates(
            UUID zoneId,
            UUID equipmentTypeId
    ) {
        if (equipmentTypeId == null) {
            return List.of();
        }

        UUID requestId = UUID.randomUUID();

        var future = consumer.registerRequest(requestId);

        try {
            producer.requestCandidates(
                    requestId,
                    zoneId,
                    equipmentTypeId
            );

            MasterAssignmentCandidatesResponseEvent response =
                    future.get(50, TimeUnit.SECONDS);

            if (response.getError() != null) {
                throw new ApiException(
                        HttpStatus.BAD_GATEWAY,
                        response.getError()
                );
            }

            return response.getMasterIds() == null
                    ? List.of()
                    : response.getMasterIds();

        } catch (TimeoutException e) {
            throw new ApiException(
                    HttpStatus.GATEWAY_TIMEOUT,
                    "User Service не ответил на запрос подходящих мастеров"
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Ожидание ответа User Service прервано"
            );

        } catch (ApiException e) {
            throw e;

        } catch (Exception e) {
            throw new ApiException(
                    HttpStatus.BAD_GATEWAY,
                    "Не удалось получить список подходящих мастеров"
            );

        } finally {
            consumer.removeRequest(requestId);
        }
    }
}