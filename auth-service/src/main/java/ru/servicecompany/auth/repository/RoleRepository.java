package ru.servicecompany.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.servicecompany.auth.entity.Role;
import ru.servicecompany.auth.entity.RoleName;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByName(RoleName name);

}