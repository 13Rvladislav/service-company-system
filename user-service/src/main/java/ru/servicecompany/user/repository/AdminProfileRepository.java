package ru.servicecompany.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.servicecompany.user.entity.AdminProfile;

import java.util.Optional;
import java.util.UUID;

public interface AdminProfileRepository extends JpaRepository<AdminProfile, UUID> {

    Optional<AdminProfile> findByAuthUserId(UUID authUserId);

    boolean existsByAuthUserId(UUID authUserId);

    boolean existsByEmployeeNumber(String employeeNumber);
}