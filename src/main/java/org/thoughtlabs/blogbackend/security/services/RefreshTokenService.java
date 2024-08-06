package org.thoughtlabs.blogbackend.security.services;

import jakarta.transaction.Transactional;
import org.thoughtlabs.blogbackend.models.RefreshToken;
import org.thoughtlabs.blogbackend.repositories.RefreshTokenRepository;
import org.thoughtlabs.blogbackend.repositories.UserRepository;
import org.thoughtlabs.blogbackend.security.exception.TokenRefreshException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Value("${unish.app.jwtRefreshExpirationMs}")
    private Long refreshTokenDurationMs;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken createRefreshToken(Long userId) {

        RefreshToken existingToken = refreshTokenRepository.findByUserId(userId).orElse(null);

        if(existingToken != null) {
            existingToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
            existingToken.setToken(UUID.randomUUID().toString());
            return refreshTokenRepository.save(existingToken);
        } else {
            RefreshToken refreshToken = new RefreshToken();
            refreshToken.setUser(userRepository.findById(userId).get());
            refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
            refreshToken.setToken(UUID.randomUUID().toString());
            return refreshTokenRepository.save(refreshToken);
        }
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(), "Refresh token was expired. Please make a new login request");
        }

        return token;
    }

    @Transactional
    public int deleteUserById(Long userId) {
        return refreshTokenRepository.deleteByUser(userRepository.findById(userId).get());
    }
}
