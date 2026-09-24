package ru.servicecompany.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.servicecompany.user.entity.City;

import java.util.UUID;

public interface CityRepository extends JpaRepository<City, UUID> {

    boolean existsByName(String name);

}