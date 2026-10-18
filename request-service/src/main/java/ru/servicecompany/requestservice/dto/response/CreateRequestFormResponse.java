package ru.servicecompany.requestservice.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record CreateRequestFormResponse(

        ClientProfileResponse client,

        AddressResponse address,

        List<UserEquipmentResponse> equipment

) {
}