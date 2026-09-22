package ru.servicecompany.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.servicecompany.user.entity.MasterProfile;

import java.util.Optional;
import java.util.UUID;

public interface MasterProfileRepository extends JpaRepository<MasterProfile, UUID> {

    Optional<MasterProfile> findByAuthUserId(UUID authUserId);

    boolean existsByAuthUserId(UUID authUserId);

    boolean existsByEmployeeNumber(String employeeNumber);
}