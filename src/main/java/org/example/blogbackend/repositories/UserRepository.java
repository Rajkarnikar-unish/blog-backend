package org.example.blogbackend.repositories;

import org.example.blogbackend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    @Query(value = "SELECT u.*, r.name FROM users u INNER JOIN user_roles ur ON u.id = ur.user_id INNER JOIN roles r ON ur.role_id = r.id WHERE r.name = :roleName", nativeQuery = true)
    Optional<List<User>> getAllUsersByRole(String roleName);

    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
}
