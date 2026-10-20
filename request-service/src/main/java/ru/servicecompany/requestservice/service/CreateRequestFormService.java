package ru.servicecompany.requestservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.servicecompany.requestservice.config.JwtUserPrincipal;
import ru.servicecompany.requestservice.dto.response.AddressResponse;
import ru.servicecompany.requestservice.dto.response.ClientProfileResponse;
import ru.servicecompany.requestservice.dto.response.CreateRequestFormResponse;
import ru.servicecompany.requestservice.dto.response.UserEquipmentResponse;
import ru.servicecompany.requestservice.kafka.AddressByIdRequestService;
import ru.servicecompany.requestservice.kafka.EquipmentRequestService;
import ru.servicecompany.requestservice.kafka.ProfileRequestService;
import ru.servicecompany.requestservice.kafka.event.EquipmentResponseEvent;
import ru.servicecompany.requestservice.kafka.event.ProfileResponseEvent;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class CreateRequestFormService {

    private final ProfileRequestService profileRequestService;
    private final EquipmentRequestService equipmentRequestService;
    private final AddressByIdRequestService addressByIdRequestService;

    public CreateRequestFormResponse getCreateForm(
            JwtUserPrincipal principal
    ) {

        CompletableFuture<ProfileResponseEvent> profileFuture =
                CompletableFuture.supplyAsync(() ->
                        profileRequestService.getProfile(
                                principal.userId(),
                                principal.role()
                        )
                );

        CompletableFuture<EquipmentResponseEvent> equipmentFuture =
                CompletableFuture.supplyAsync(() ->
                        equipmentRequestService.getEquipment(
                                principal.userId()
                        )
                );

        ProfileResponseEvent profile = profileFuture.join();
        EquipmentResponseEvent equipment = equipmentFuture.join();

        ClientProfileResponse client =
                ClientProfileResponse.builder()
                        .id(profile.getId())
                        .authUserId(profile.getAuthUserId())
                        .role(profile.getRole())
                        .firstName(profile.getFirstName())
                        .lastName(profile.getLastName())
                        .middleName(profile.getMiddleName())
                        .phone(profile.getPhone())
                        .hasAvatar(profile.getHasAvatar())
                        .cityId(profile.getCityId())
                        .cityName(profile.getCityName())
                        .streetId(profile.getStreetId())
                        .streetName(profile.getStreetName())
                        .houseId(profile.getHouseId())
                        .houseNumber(profile.getHouseNumber())
                        .apartment(profile.getApartment())
                        .employeeNumber(profile.getEmployeeNumber())
                        .status(profile.getStatus())
                        .build();

        AddressResponse address =
                AddressResponse.builder()
                        .cityId(profile.getCityId())
                        .cityName(profile.getCityName())
                        .streetId(profile.getStreetId())
                        .streetName(profile.getStreetName())
                        .houseId(profile.getHouseId())
                        .houseNumber(profile.getHouseNumber())
                        .apartment(profile.getApartment())
                        .zoneId(profile.getZoneId())
                        .build();

        List<UserEquipmentResponse> equipmentList =
                equipment.getEquipment()
                        .stream()
                        .map(item -> UserEquipmentResponse.builder()
                                .id(item.getId())
                                .equipmentCatalogId(item.getEquipmentCatalogId())
                                .equipmentTypeId(item.getEquipmentTypeId())
                                .equipmentTypeName(item.getEquipmentTypeName())
                                .manufacturer(item.getManufacturer())
                                .model(item.getModel())
                                .description(item.getDescription())
                                .serialNumber(item.getSerialNumber())
                                .installationDate(item.getInstallationDate())
                                .build())
                        .toList();

        return CreateRequestFormResponse.builder()
                .client(client)
                .address(address)
                .equipment(equipmentList)
                .build();
    }
}