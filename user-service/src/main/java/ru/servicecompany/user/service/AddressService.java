
package ru.servicecompany.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.servicecompany.user.common.exception.ApiException;
import ru.servicecompany.user.dto.request.*;
import ru.servicecompany.user.dto.response.CityResponse;
import ru.servicecompany.user.dto.response.HouseResponse;
import ru.servicecompany.user.dto.response.StreetResponse;
import ru.servicecompany.user.entity.City;
import ru.servicecompany.user.entity.House;
import ru.servicecompany.user.entity.Street;
import ru.servicecompany.user.entity.Zone;
import ru.servicecompany.user.repository.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressService {

    private final CityRepository cityRepository;
    private final StreetRepository streetRepository;
    private final HouseRepository houseRepository;
    private final ZoneRepository zoneRepository;
    private final UserProfileRepository userProfileRepository;
    private final AuditService auditService;

    // =========================
    // CITY
    // =========================

    /**
     * Создать город.
     */
    @Transactional
    public CityResponse createCity(CreateCityRequest request) {

        if (cityRepository.existsByName(request.getName())) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Город уже существует"
            );
        }

        City city = new City();
        city.setName(request.getName());

        City savedCity = cityRepository.save(city);

        log.info(
                "BUSINESS_EVENT event=CITY_CREATED cityId={}",
                savedCity.getId()
        );

        auditService.logSuccess(
                "CITY_CREATED",
                "CITY",
                savedCity.getId(),
                "operation=CREATE"
        );

        return map(savedCity);
    }

    /**
     * Получить все города.
     */
    @Transactional(readOnly = true)
    public List<CityResponse> getCities() {
        return cityRepository.findAll()
                .stream()
                .map(this::map)
                .toList();
    }

    /**
     * Обновить город.
     */
    @Transactional
    public CityResponse updateCity(UUID id, UpdateCityRequest request) {

        City city = cityRepository.findById(id)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Город не найден"
                ));

        city.setName(request.getName());

        City savedCity = cityRepository.save(city);

        log.info(
                "BUSINESS_EVENT event=CITY_UPDATED cityId={}",
                savedCity.getId()
        );

        auditService.logSuccess(
                "CITY_UPDATED",
                "CITY",
                savedCity.getId(),
                "operation=UPDATE"
        );

        return map(savedCity);
    }

    /**
     * Удалить город.
     */
    @Transactional
    public void deleteCity(UUID id) {

        City city = cityRepository.findById(id)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Город не найден"
                ));

        if (streetRepository.existsByCityId(id)) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Невозможно удалить город: в нём существуют улицы"
            );
        }

        cityRepository.delete(city);

        log.info(
                "BUSINESS_EVENT event=CITY_DELETED cityId={}",
                id
        );

        auditService.logSuccess(
                "CITY_DELETED",
                "CITY",
                id,
                "operation=DELETE"
        );
    }

    // =========================
    // STREET
    // =========================

    /**
     * Создать улицу.
     */
    @Transactional
    public StreetResponse createStreet(CreateStreetRequest request) {

        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Город не найден"
                ));

        Zone zone = zoneRepository.findById(request.getZoneId())
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Зона не найдена"
                ));

        Street street = new Street();
        street.setName(request.getName());
        street.setCity(city);
        street.setZone(zone);

        Street savedStreet = streetRepository.save(street);

        log.info(
                "BUSINESS_EVENT event=STREET_CREATED streetId={} cityId={} zoneId={}",
                savedStreet.getId(),
                city.getId(),
                zone.getId()
        );

        auditService.logSuccess(
                "STREET_CREATED",
                "STREET",
                savedStreet.getId(),
                "operation=CREATE;cityId=" + city.getId()
                        + ";zoneId=" + zone.getId()
        );

        return map(savedStreet);
    }

    /**
     * Получить улицы города.
     */
    @Transactional(readOnly = true)
    public List<StreetResponse> getStreets(UUID cityId) {

        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Город не найден"
                ));

        return streetRepository.findByCity(city)
                .stream()
                .map(this::map)
                .toList();
    }

    /**
     * Обновить улицу.
     */
    @Transactional
    public StreetResponse updateStreet(
            UUID id,
            UpdateStreetRequest request
    ) {

        Street street = streetRepository.findById(id)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Улица не найдена"
                ));

        Zone zone = zoneRepository.findById(request.getZoneId())
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Зона не найдена"
                ));

        street.setName(request.getName());
        street.setZone(zone);

        Street savedStreet = streetRepository.save(street);

        log.info(
                "BUSINESS_EVENT event=STREET_UPDATED streetId={} cityId={} zoneId={}",
                savedStreet.getId(),
                savedStreet.getCity().getId(),
                zone.getId()
        );

        auditService.logSuccess(
                "STREET_UPDATED",
                "STREET",
                savedStreet.getId(),
                "operation=UPDATE;cityId=" + savedStreet.getCity().getId()
                        + ";zoneId=" + zone.getId()
        );

        return map(savedStreet);
    }

    /**
     * Удалить улицу.
     */
    @Transactional
    public void deleteStreet(UUID id) {

        Street street = streetRepository.findById(id)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Улица не найдена"
                ));

        if (houseRepository.existsByStreetId(id)) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Невозможно удалить улицу: на ней существуют дома"
            );
        }

        UUID cityId = street.getCity().getId();

        streetRepository.delete(street);

        log.info(
                "BUSINESS_EVENT event=STREET_DELETED streetId={} cityId={}",
                id,
                cityId
        );

        auditService.logSuccess(
                "STREET_DELETED",
                "STREET",
                id,
                "operation=DELETE;cityId=" + cityId
        );
    }

    // =========================
    // HOUSE
    // =========================

    /**
     * Создать дом.
     */
    @Transactional
    public HouseResponse createHouse(CreateHouseRequest request) {

        Street street = streetRepository.findById(request.getStreetId())
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Улица не найдена"
                ));

        House house = new House();
        house.setNumber(request.getNumber());
        house.setStreet(street);

        House savedHouse = houseRepository.save(house);

        log.info(
                "BUSINESS_EVENT event=HOUSE_CREATED houseId={} streetId={} cityId={}",
                savedHouse.getId(),
                street.getId(),
                street.getCity().getId()
        );

        auditService.logSuccess(
                "HOUSE_CREATED",
                "HOUSE",
                savedHouse.getId(),
                "operation=CREATE;streetId=" + street.getId()
                        + ";cityId=" + street.getCity().getId()
        );

        return map(savedHouse);
    }

    /**
     * Получить дома улицы.
     */
    @Transactional(readOnly = true)
    public List<HouseResponse> getHouses(UUID streetId) {

        Street street = streetRepository.findById(streetId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Улица не найдена"
                ));

        return houseRepository.findByStreet(street)
                .stream()
                .map(this::map)
                .toList();
    }

    /**
     * Обновить дом.
     */
    @Transactional
    public HouseResponse updateHouse(
            UUID id,
            UpdateHouseRequest request
    ) {

        House house = houseRepository.findById(id)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Дом не найден"
                ));

        house.setNumber(request.getNumber());

        House savedHouse = houseRepository.save(house);

        log.info(
                "BUSINESS_EVENT event=HOUSE_UPDATED houseId={} streetId={}",
                savedHouse.getId(),
                savedHouse.getStreet().getId()
        );

        auditService.logSuccess(
                "HOUSE_UPDATED",
                "HOUSE",
                savedHouse.getId(),
                "operation=UPDATE;streetId=" + savedHouse.getStreet().getId()
        );

        return map(savedHouse);
    }

    /**
     * Удалить дом.
     */
    @Transactional
    public void deleteHouse(UUID id) {

        House house = houseRepository.findById(id)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Дом не найден"
                ));

        if (userProfileRepository.existsByHouse_Id(id)) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Невозможно удалить дом: в доме зарегистрированы жильцы"
            );
        }

        UUID streetId = house.getStreet().getId();

        houseRepository.delete(house);

        log.info(
                "BUSINESS_EVENT event=HOUSE_DELETED houseId={} streetId={}",
                id,
                streetId
        );

        auditService.logSuccess(
                "HOUSE_DELETED",
                "HOUSE",
                id,
                "operation=DELETE;streetId=" + streetId
        );
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

    /**
     * Получить дом по ID вместе с информацией об адресе.
     */
    @Transactional(readOnly = true)
    public HouseResponse getHouseById(UUID houseId) {

        House house = houseRepository.findById(houseId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Дом не найден"
                ));

        return HouseResponse.builder()
                .id(house.getId())
                .number(house.getNumber())
                .streetId(house.getStreet().getId())
                .streetName(house.getStreet().getName())
                .cityId(house.getStreet().getCity().getId())
                .cityName(house.getStreet().getCity().getName())
                .zoneId(house.getStreet().getZone().getId())
                .build();
    }
}
