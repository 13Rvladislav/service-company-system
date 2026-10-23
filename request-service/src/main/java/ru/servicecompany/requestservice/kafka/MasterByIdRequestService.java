package ru.servicecompany.requestservice.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.servicecompany.requestservice.common.exception.ApiException;
import ru.servicecompany.requestservice.dto.response.MasterResponse;
import ru.servicecompany.requestservice.kafka.event.MasterByIdResponseEvent;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
public class MasterByIdRequestService {

    private final MasterByIdRequestProducer producer;
    private final MasterByIdResponseConsumer consumer;

    public MasterResponse getMasterById(UUID masterId) {

        if (masterId == null) {
            return null;
        }

        MasterByIdResponseEvent response =
                sendRequest(masterId, null);

        return new MasterResponse(
                response.getMasterId(),
                response.getFirstName(),
                response.getLastName(),
                response.getMiddleName(),
                response.getPhone()
        );
    }

    /**
     * Проверяет, соответствует ли специализация мастера
     * типу оборудования.
     *
     * Зона обслуживания и активная нагрузка не проверяются.
     */
    public boolean isSuitable(
            UUID masterId,
            UUID equipmentTypeId
    ) {
        if (masterId == null || equipmentTypeId == null) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Необходимо указать мастера и тип оборудования"
            );
        }

        MasterByIdResponseEvent response =
                sendRequest(masterId, equipmentTypeId);

        return Boolean.TRUE.equals(response.getSuitable());
    }

    private MasterByIdResponseEvent sendRequest(
            UUID masterId,
            UUID equipmentTypeId
    ) {
        UUID requestId = UUID.randomUUID();

        var future = consumer.registerRequest(requestId);

        try {
            producer.requestMaster(
                    requestId,
                    masterId,
                    equipmentTypeId
            );

            MasterByIdResponseEvent response =
                    future.get(50, TimeUnit.SECONDS);

            if (response.getError() != null) {
                throw new ApiException(
                        HttpStatus.BAD_GATEWAY,
                        response.getError()
                );
            }

            return response;

        } catch (TimeoutException e) {
            throw new ApiException(
                    HttpStatus.GATEWAY_TIMEOUT,
                    "User Service не ответил на запрос мастера"
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
                    "Не удалось получить мастера по ID"
            );

        } finally {
            consumer.removeRequest(requestId);
        }
    }
}