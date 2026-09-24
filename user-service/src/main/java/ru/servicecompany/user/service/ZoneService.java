package ru.servicecompany.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.servicecompany.user.common.exception.ApiException;
import ru.servicecompany.user.dto.request.CreateZoneRequest;
import ru.servicecompany.user.dto.response.ZoneResponse;
import ru.servicecompany.user.entity.Zone;
import ru.servicecompany.user.repository.ZoneRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ZoneService {

    private final ZoneRepository zoneRepository;

    /**
     * Создать новую зону.
     */
    public ZoneResponse create(CreateZoneRequest request) {

        if (zoneRepository.existsByName(request.getName())) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Зона уже существует"
            );
        }

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
     * Entity → DTO
     */
    private ZoneResponse map(Zone zone) {

        return ZoneResponse.builder()
                .id(zone.getId())
                .name(zone.getName())
                .description(zone.getDescription())
                .build();
    }
}