package ru.servicecompany.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.servicecompany.user.entity.UserProfile;

import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

    Optional<UserProfile> findByAuthUserId(UUID authUserId);

    boolean existsByAuthUserId(UUID authUserId);

    boolean existsByHouse_Id(UUID houseId);
}