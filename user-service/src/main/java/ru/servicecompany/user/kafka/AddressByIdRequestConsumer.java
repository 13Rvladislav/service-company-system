package ru.servicecompany.user.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.servicecompany.user.dto.response.HouseResponse;
import ru.servicecompany.user.kafka.event.AddressByIdRequestEvent;
import ru.servicecompany.user.kafka.event.AddressByIdResponseEvent;
import ru.servicecompany.user.service.AddressService;

@Slf4j
@Component
@RequiredArgsConstructor
public class AddressByIdRequestConsumer {

    private final AddressService addressService;
    private final AddressByIdResponseProducer responseProducer;

    @KafkaListener(
            topics = KafkaTopics.ADDRESS_BY_ID_REQUEST,
            groupId = "user-service-address-by-id-group",
            containerFactory = "addressByIdRequestKafkaListenerContainerFactory"
    )
    public void consume(AddressByIdRequestEvent event) {

        if (event == null
                || event.getRequestId() == null
                || event.getAddressId() == null) {

            log.error("USER SERVICE: некорректный запрос адреса");
            return;
        }

        try {
            HouseResponse house = addressService.getHouseById(
                    event.getAddressId()
            );

            AddressByIdResponseEvent response =
                    AddressByIdResponseEvent.builder()
                            .requestId(event.getRequestId())
                            .addressId(house.getId())
                            .houseId(house.getId())
                            .houseNumber(house.getNumber())
                            .streetId(house.getStreetId())
                            .streetName(house.getStreetName())
                            .cityId(house.getCityId())
                            .cityName(house.getCityName())
                            .zoneId(house.getZoneId())
                            .build();

            responseProducer.sendResponse(response);

        } catch (Exception e) {

            log.error(
                    "USER SERVICE: не удалось получить адрес, requestId={}, addressId={}",
                    event.getRequestId(),
                    event.getAddressId(),
                    e
            );

            AddressByIdResponseEvent response =
                    AddressByIdResponseEvent.builder()
                            .requestId(event.getRequestId())
                            .addressId(event.getAddressId())
                            .error("Адрес не найден")
                            .build();

            responseProducer.sendResponse(response);
        }
    }
}