package org.thoughtlabs.blogbackend.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.thoughtlabs.blogbackend.security.exception.JwtValidationException;
import org.thoughtlabs.blogbackend.security.services.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${unish.app.secret}")
    private String jwtSecret;

    @Value("${unish.app.jwtExpirationMs}")
    private Integer jwtExpirationMs;

    public String generateJwtToken(UserDetailsImpl userPrincipal) {
        return generateTokenFromUsername(userPrincipal.getUsername());
    }

    public String generateTokenFromUsername(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS512)
                .compact();
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser().setSigningKey(jwtSecret).build().parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateJwtToken(String authToken) throws JwtValidationException {
        try {
            Jwts.parser().setSigningKey(key()).build().parse(authToken);
            return true;
        } catch (MalformedJwtException e) {
            throw new JwtValidationException("Invalid JWT Token: " + e.getMessage());
        }catch (ExpiredJwtException e) {
            throw new JwtValidationException("JWT token is expired: " + e.getMessage());
        }catch (UnsupportedJwtException e) {
            throw new JwtValidationException("JWT token is unsupported: " + e.getMessage());
        }catch (IllegalArgumentException e) {
            throw new JwtValidationException("JWT claims string is empty: " + e.getMessage());
        }
    }
}
