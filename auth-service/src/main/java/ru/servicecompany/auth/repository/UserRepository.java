package ru.servicecompany.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.servicecompany.auth.entity.User;

import java.util.Optional;
import java.util.UUID;

// репозиторий для USER
public interface UserRepository extends JpaRepository<User, UUID> {
    // поиск по email
    Optional<User> findByEmail(String email);

    // Встречается ли email
    boolean existsByEmail(String email);

    // Встречается ли номер email
    boolean existsByPhone(String phone);


    @Query("""
    SELECT u
    FROM User u
    JOIN FETCH u.role
    WHERE u.email = :email
""")
    Optional<User> findByEmailWithRole(String email);
}