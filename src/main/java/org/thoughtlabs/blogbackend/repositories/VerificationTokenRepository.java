package org.thoughtlabs.blogbackend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;
import org.thoughtlabs.blogbackend.models.User;
import org.thoughtlabs.blogbackend.models.VerificationToken;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends ListCrudRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByToken(String token);

    void deleteByUser(User user);
}
