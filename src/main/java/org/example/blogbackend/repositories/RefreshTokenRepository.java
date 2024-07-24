package org.example.blogbackend.repositories;

import org.example.blogbackend.models.RefreshToken;
import org.example.blogbackend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    @Query(value = "SELECT * FROM refresh_token WHERE user_id = :userId", nativeQuery = true)
    Optional<RefreshToken> findByUserId(Long userId);

    @Modifying
    int deleteByUser(User user);
}
