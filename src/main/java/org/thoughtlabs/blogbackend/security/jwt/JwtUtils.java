package org.thoughtlabs.blogbackend.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.thoughtlabs.blogbackend.config.AppProperties;
import org.thoughtlabs.blogbackend.security.exception.JwtValidationException;
import org.thoughtlabs.blogbackend.security.services.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    private AppProperties appProperties;

    @Value("${app.auth.tokenSecret}")
    private String jwtSecret;

    @Value("${app.auth.jwtExpirationMs}")
    private Integer jwtExpirationMs;

    public JwtUtils(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public String generateJwtToken(UserDetailsImpl userPrincipal) {
        return generateTokenFromUsername(userPrincipal.getUsername());
    }

    public String generateTokenFromUsername(String username) {
        return generateToken(username);
    }
//    return Jwts.builder()
//            .setSubject(username)
//                .setIssuedAt(new Date())
//            .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
//            .signWith(key(), SignatureAlgorithm.HS512)
//            .compact();

    public String generateEmailVerificationToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 24*60*60*1000))
                .signWith(key(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String generatePasswordResetToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 24*60*60*1000))
                .signWith(key(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String getEmailFromPasswordResetToken(String token) {
        return Jwts.parser()
                .setSigningKey(jwtSecret)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String generateTokenFromOAuth2Username(Authentication authentication) {
        return generateToken(authentication);
    }

    public String generateToken(Object principal) {
        String username;

        if(principal instanceof String) {
            username = (String) principal;
        } else if(principal instanceof Authentication) {
            UserDetailsImpl userDetailsImpl = (UserDetailsImpl)  ((Authentication) principal).getPrincipal();
            username = userDetailsImpl.getUsername();
        } else {
            throw new IllegalArgumentException("Invalid Principal Type!");
        }

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
        return Jwts.parser()
                .setSigningKey(jwtSecret)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateJwtToken(String authToken) throws JwtValidationException {
        try {
            Jwts.parser().setSigningKey(key()).build().parse(authToken);
//            Jwts.parser().setSigningKey(appProperties.getAuth().getTokenSecret())
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
