package ru.servicecompany.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.servicecompany.user.entity.City;
import ru.servicecompany.user.entity.Street;

import java.util.List;
import java.util.UUID;

public interface StreetRepository extends JpaRepository<Street, UUID> {

    List<Street> findByCity(City city);

    List<Street> findByCityIdOrderByNameAsc(UUID cityId);

    boolean existsByZoneId(UUID zoneId);
}