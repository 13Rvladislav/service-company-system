package ru.servicecompany.requestservice.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.servicecompany.requestservice.common.exception.ApiException;
import ru.servicecompany.requestservice.kafka.event.EquipmentResponseEvent;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
public class EquipmentRequestService {

    private final EquipmentRequestProducer producer;
    private final EquipmentResponseConsumer consumer;

    public EquipmentResponseEvent getEquipment(UUID authUserId) {

        UUID requestId = UUID.randomUUID();

        CompletableFuture<EquipmentResponseEvent> future =
                consumer.registerRequest(requestId);

        try {
            producer.requestEquipment(
                    requestId,
                    authUserId
            );

            EquipmentResponseEvent response =
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
                    "Не удалось получить оборудование пользователя"
            );

        } finally {
            consumer.removeRequest(requestId);
        }
    }
}