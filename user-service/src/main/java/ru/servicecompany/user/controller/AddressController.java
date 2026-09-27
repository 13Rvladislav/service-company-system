package ru.servicecompany.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.servicecompany.user.dto.request.*;
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


    @PutMapping("/cities/{id}")
    public CityResponse updateCity(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateCityRequest request
    ) {
        return addressService.updateCity(id, request);
    }

    @DeleteMapping("/cities/{id}")
    public void deleteCity(@PathVariable UUID id) {
        addressService.deleteCity(id);
    }

    @PutMapping("/streets/{id}")
    public StreetResponse updateStreet(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateStreetRequest request
    ) {
        return addressService.updateStreet(id, request);
    }

    @DeleteMapping("/streets/{id}")
    public void deleteStreet(@PathVariable UUID id) {
        addressService.deleteStreet(id);
    }

    @PutMapping("/houses/{id}")
    public HouseResponse updateHouse(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateHouseRequest request
    ) {
        return addressService.updateHouse(id, request);
    }

    @DeleteMapping("/houses/{id}")
    public void deleteHouse(@PathVariable UUID id) {
        addressService.deleteHouse(id);
    }
}