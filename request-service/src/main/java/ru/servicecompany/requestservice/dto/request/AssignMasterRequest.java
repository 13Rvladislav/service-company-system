package ru.servicecompany.requestservice.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignMasterRequest(
        @NotNull UUID masterId
) {
}