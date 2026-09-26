package ru.servicecompany.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.servicecompany.user.common.exception.ApiException;
import ru.servicecompany.user.dto.request.CreateZoneRequest;
import ru.servicecompany.user.dto.request.UpdateZoneRequest;
import ru.servicecompany.user.dto.response.ZoneResponse;
import ru.servicecompany.user.entity.Zone;
import ru.servicecompany.user.repository.StreetRepository;
import ru.servicecompany.user.repository.ZoneRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ZoneService {

    private final ZoneRepository zoneRepository;
    private final StreetRepository streetRepository;

    /**
     * Создать зону.
     */
    public ZoneResponse create(CreateZoneRequest request) {

        Zone zone = new Zone();
        zone.setName(request.getName());
        zone.setDescription(request.getDescription());

        return map(zoneRepository.save(zone));
    }

    /**
     * Получить все зоны.
     */
    public List<ZoneResponse> getAll() {

        return zoneRepository.findAll()
                .stream()
                .map(this::map)
                .toList();
    }

    /**
     * Обновить название и описание зоны.
     */
    public ZoneResponse update(UUID id, UpdateZoneRequest request) {

        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Зона не найдена"
                ));

        zone.setName(request.getName());
        zone.setDescription(request.getDescription());

        return map(zoneRepository.save(zone));
    }

    /**
     * Удалить зону.
     * Запрещено, если к зоне привязаны улицы.
     */
    public void delete(UUID id) {

        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Зона не найдена"
                ));

        if (streetRepository.existsByZoneId(id)) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Невозможно удалить зону: к ней привязаны улицы"
            );
        }

        zoneRepository.delete(zone);
    }

    /**
     * Entity -> DTO.
     */
    private ZoneResponse map(Zone zone) {

        return ZoneResponse.builder()
                .id(zone.getId())
                .name(zone.getName())
                .description(zone.getDescription())
                .build();
    }
}