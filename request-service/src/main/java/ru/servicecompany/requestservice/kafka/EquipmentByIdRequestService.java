package ru.servicecompany.requestservice.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.servicecompany.requestservice.common.exception.ApiException;
import ru.servicecompany.requestservice.kafka.event.EquipmentByIdResponseEvent;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
public class EquipmentByIdRequestService {

    private final EquipmentByIdRequestProducer producer;
    private final EquipmentByIdResponseConsumer consumer;

    public EquipmentByIdResponseEvent getEquipmentById(
            UUID authUserId,
            UUID equipmentId
    ) {
        UUID requestId = UUID.randomUUID();

        var future = consumer.registerRequest(requestId);

        try {
            producer.requestEquipmentById(
                    requestId,
                    authUserId,
                    equipmentId
            );

            EquipmentByIdResponseEvent response =
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
                    "Equipment Service не ответил на запрос оборудования"
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Ожидание ответа Equipment Service прервано"
            );

        } catch (ApiException e) {
            throw e;

        } catch (Exception e) {
            throw new ApiException(
                    HttpStatus.BAD_GATEWAY,
                    "Не удалось получить оборудование по ID"
            );

        } finally {
            consumer.removeRequest(requestId);
        }
    }
}