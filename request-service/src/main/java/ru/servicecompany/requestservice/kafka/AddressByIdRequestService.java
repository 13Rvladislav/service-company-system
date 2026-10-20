package ru.servicecompany.requestservice.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.servicecompany.requestservice.common.exception.ApiException;
import ru.servicecompany.requestservice.dto.response.AddressResponse;
import ru.servicecompany.requestservice.kafka.event.AddressByIdResponseEvent;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
public class AddressByIdRequestService {

    private final AddressByIdRequestProducer producer;
    private final AddressByIdResponseConsumer consumer;

    public AddressResponse getAddressById(UUID addressId) {

        if (addressId == null) {
            return null;
        }

        UUID requestId = UUID.randomUUID();

        var future = consumer.registerRequest(requestId);

        try {
            producer.requestAddress(requestId, addressId);

            AddressByIdResponseEvent response =
                    future.get(50, TimeUnit.SECONDS);

            if (response.getError() != null) {
                throw new ApiException(
                        HttpStatus.BAD_GATEWAY,
                        response.getError()
                );
            }

            return AddressResponse.builder()
                    .houseId(response.getHouseId())
                    .houseNumber(response.getHouseNumber())
                    .streetId(response.getStreetId())
                    .streetName(response.getStreetName())
                    .cityId(response.getCityId())
                    .cityName(response.getCityName())
                    .build();

        } catch (TimeoutException e) {
            throw new ApiException(
                    HttpStatus.GATEWAY_TIMEOUT,
                    "User Service не ответил на запрос адреса"
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Ожидание ответа User Service было прервано"
            );

        } catch (ApiException e) {
            throw e;

        } catch (Exception e) {
            throw new ApiException(
                    HttpStatus.BAD_GATEWAY,
                    "Не удалось получить адрес по ID"
            );

        } finally {
            consumer.removeRequest(requestId);
        }
    }
}