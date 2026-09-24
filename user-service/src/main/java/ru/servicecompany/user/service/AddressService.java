package ru.servicecompany.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.servicecompany.user.common.exception.ApiException;
import ru.servicecompany.user.dto.request.CreateCityRequest;
import ru.servicecompany.user.dto.request.CreateHouseRequest;
import ru.servicecompany.user.dto.request.CreateStreetRequest;
import ru.servicecompany.user.dto.response.CityResponse;
import ru.servicecompany.user.dto.response.HouseResponse;
import ru.servicecompany.user.dto.response.StreetResponse;
import ru.servicecompany.user.entity.City;
import ru.servicecompany.user.entity.House;
import ru.servicecompany.user.entity.Street;
import ru.servicecompany.user.entity.Zone;
import ru.servicecompany.user.repository.CityRepository;
import ru.servicecompany.user.repository.HouseRepository;
import ru.servicecompany.user.repository.StreetRepository;
import ru.servicecompany.user.repository.ZoneRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final CityRepository cityRepository;
    private final StreetRepository streetRepository;
    private final HouseRepository houseRepository;
    private final ZoneRepository zoneRepository;

    // =========================
    // CITY
    // =========================

    public CityResponse createCity(CreateCityRequest request) {

        if (cityRepository.existsByName(request.getName())) {
            throw new ApiException(HttpStatus.CONFLICT, "Город уже существует");
        }

        City city = new City();
        city.setName(request.getName());

        return map(cityRepository.save(city));
    }

    public List<CityResponse> getCities() {
        return cityRepository.findAll()
                .stream()
                .map(this::map)
                .toList();
    }

    // =========================
    // STREET
    // =========================

    public StreetResponse createStreet(CreateStreetRequest request) {

        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Город не найден"));

        Zone zone = zoneRepository.findById(request.getZoneId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Зона не найдена"));

        Street street = new Street();
        street.setName(request.getName());
        street.setCity(city);
        street.setZone(zone);

        return map(streetRepository.save(street));
    }

    public List<StreetResponse> getStreets(UUID cityId) {

        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Город не найден"));

        return streetRepository.findByCity(city)
                .stream()
                .map(this::map)
                .toList();
    }

    // =========================
    // HOUSE
    // =========================

    public HouseResponse createHouse(CreateHouseRequest request) {

        Street street = streetRepository.findById(request.getStreetId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Улица не найдена"));

        House house = new House();
        house.setNumber(request.getNumber());
        house.setStreet(street);

        return map(houseRepository.save(house));
    }

    public List<HouseResponse> getHouses(UUID streetId) {

        Street street = streetRepository.findById(streetId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Улица не найдена"));

        return houseRepository.findByStreet(street)
                .stream()
                .map(this::map)
                .toList();
    }

    // =========================
    // MAPPERS
    // =========================

    private CityResponse map(City city) {
        return CityResponse.builder()
                .id(city.getId())
                .name(city.getName())
                .build();
    }

    private StreetResponse map(Street street) {
        return StreetResponse.builder()
                .id(street.getId())
                .name(street.getName())
                .cityId(street.getCity().getId())
                .cityName(street.getCity().getName())
                .zoneId(street.getZone().getId())
                .zoneName(street.getZone().getName())
                .build();
    }

    private HouseResponse map(House house) {
        return HouseResponse.builder()
                .id(house.getId())
                .number(house.getNumber())
                .streetId(house.getStreet().getId())
                .streetName(house.getStreet().getName())
                .build();
    }
}