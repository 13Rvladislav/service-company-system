
package ru.servicecompany.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.servicecompany.user.common.exception.ApiException;
import ru.servicecompany.user.dto.request.AssignMasterZonesRequest;
import ru.servicecompany.user.dto.request.CreateZoneRequest;
import ru.servicecompany.user.dto.request.UpdateZoneRequest;
import ru.servicecompany.user.dto.response.ZoneResponse;
import ru.servicecompany.user.entity.MasterProfile;
import ru.servicecompany.user.entity.MasterZone;
import ru.servicecompany.user.entity.Zone;
import ru.servicecompany.user.repository.MasterProfileRepository;
import ru.servicecompany.user.repository.MasterZoneRepository;
import ru.servicecompany.user.repository.StreetRepository;
import ru.servicecompany.user.repository.ZoneRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ZoneService {

    private final ZoneRepository zoneRepository;
    private final StreetRepository streetRepository;
    private final MasterProfileRepository masterProfileRepository;
    private final MasterZoneRepository masterZoneRepository;

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
    @Transactional(readOnly = true)
    public List<ZoneResponse> getAll() {
        return zoneRepository.findAll()
                .stream()
                .map(this::map)
                .toList();
    }

    /**
     * Обновить название и описание зоны.
     */
    @Transactional
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
     * Удаление запрещено, если с зоной связаны улицы
     * или за зоной закреплены мастера.
     */
    @Transactional
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

        if (masterZoneRepository.existsByZone_Id(id)) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Невозможно удалить зону: за ней закреплены мастера"
            );
        }

        zoneRepository.delete(zone);
    }

    /**
     * Получить все зоны, закреплённые за мастером.
     */
    @Transactional(readOnly = true)
    public List<ZoneResponse> getMasterZones(UUID masterId) {
        MasterProfile master = findMaster(masterId);

        return masterZoneRepository.findAllByMaster_Id(master.getId())
                .stream()
                .map(MasterZone::getZone)
                .map(this::map)
                .toList();
    }

    /**
     * Полностью обновить список зон мастера.
     *
     * Переданный список заменяет все текущие назначения.
     * Пустой список снимает все назначения.
     */
    @Transactional
    public List<ZoneResponse> assignMasterZones(
            UUID masterId,
            AssignMasterZonesRequest request
    ) {
        MasterProfile master = findMaster(masterId);

        List<UUID> zoneIds = request.getZoneIds();

        if (zoneIds == null) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Список зон не может быть null"
            );
        }

        if (zoneIds.stream().anyMatch(id -> id == null)) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "ID зоны не может быть null"
            );
        }

        Set<UUID> uniqueZoneIds = new HashSet<>(zoneIds);

        if (uniqueZoneIds.size() != zoneIds.size()) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Список содержит повторяющиеся зоны"
            );
        }

        // Сначала проверяем все зоны.
        List<Zone> zones = zoneRepository.findAllById(uniqueZoneIds);

        if (zones.size() != uniqueZoneIds.size()) {
            throw new ApiException(
                    HttpStatus.NOT_FOUND,
                    "Одна или несколько зон не найдены"
            );
        }

        // Удаляем прежние назначения именно найденного профиля.
        masterZoneRepository.deleteAllByMasterIdImmediately(master.getId());

        // Сохраняем новые назначения.
        List<MasterZone> assignments = zones.stream()
                .map(zone -> new MasterZone(master, zone))
                .toList();

        masterZoneRepository.saveAll(assignments);

        return zones.stream()
                .map(this::map)
                .toList();
    }

    /**
     * Найти профиль мастера по ID профиля
     * или по authUserId из auth-service.
     */
    private MasterProfile findMaster(UUID id) {
        return masterProfileRepository.findById(id)
                .or(() -> masterProfileRepository.findByAuthUserId(id))
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Профиль мастера не найден"
                ));
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
