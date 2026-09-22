package ru.servicecompany.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.servicecompany.user.entity.DispatcherProfile;

import java.util.Optional;
import java.util.UUID;

public interface DispatcherProfileRepository extends JpaRepository<DispatcherProfile, UUID> {

    Optional<DispatcherProfile> findByAuthUserId(UUID authUserId);

    boolean existsByAuthUserId(UUID authUserId);

    boolean existsByEmployeeNumber(String employeeNumber);
}