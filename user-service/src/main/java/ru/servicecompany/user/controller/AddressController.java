package ru.servicecompany.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.servicecompany.user.dto.request.CreateCityRequest;
import ru.servicecompany.user.dto.request.CreateHouseRequest;
import ru.servicecompany.user.dto.request.CreateStreetRequest;
import ru.servicecompany.user.dto.response.CityResponse;
import ru.servicecompany.user.dto.response.HouseResponse;
import ru.servicecompany.user.dto.response.StreetResponse;
import ru.servicecompany.user.service.AddressService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    // =========================
    // CITY
    // =========================

    @PostMapping("/cities")
    public CityResponse createCity(
            @Valid @RequestBody CreateCityRequest request
    ) {
        return addressService.createCity(request);
    }

    @GetMapping("/cities")
    public List<CityResponse> getCities() {
        return addressService.getCities();
    }

    // =========================
    // STREET
    // =========================

    @PostMapping("/streets")
    public StreetResponse createStreet(
            @Valid @RequestBody CreateStreetRequest request
    ) {
        return addressService.createStreet(request);
    }

    @GetMapping("/cities/{cityId}/streets")
    public List<StreetResponse> getStreets(
            @PathVariable UUID cityId
    ) {
        return addressService.getStreets(cityId);
    }

    // =========================
    // HOUSE
    // =========================

    @PostMapping("/houses")
    public HouseResponse createHouse(
            @Valid @RequestBody CreateHouseRequest request
    ) {
        return addressService.createHouse(request);
    }

    @GetMapping("/streets/{streetId}/houses")
    public List<HouseResponse> getHouses(
            @PathVariable UUID streetId
    ) {
        return addressService.getHouses(streetId);
    }
}