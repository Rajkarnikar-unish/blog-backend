package org.thoughtlabs.blogbackend.repositories;

import org.thoughtlabs.blogbackend.models.ERole;
import org.thoughtlabs.blogbackend.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole name);
}
