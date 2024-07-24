package org.example.blogbackend.controllers;

import jakarta.validation.Valid;
import org.example.blogbackend.exceptions.EmailAlreadyExistsException;
import org.example.blogbackend.exceptions.UsernameAlreadyExistsException;
import org.example.blogbackend.models.ERole;
import org.example.blogbackend.models.RefreshToken;
import org.example.blogbackend.models.Role;
import org.example.blogbackend.models.User;
import org.example.blogbackend.payload.request.LoginRequest;
import org.example.blogbackend.payload.request.RegistrationRequest;
import org.example.blogbackend.payload.request.TokenRefreshRequest;
import org.example.blogbackend.payload.response.JwtResponse;
import org.example.blogbackend.payload.response.MessageResponse;
import org.example.blogbackend.payload.response.TokenRefreshResponse;
import org.example.blogbackend.repositories.RoleRepository;
import org.example.blogbackend.repositories.UserRepository;
import org.example.blogbackend.security.exception.TokenRefreshException;
import org.example.blogbackend.security.jwt.JwtUtils;
import org.example.blogbackend.security.services.RefreshTokenService;
import org.example.blogbackend.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    RefreshTokenService refreshTokenService;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @GetMapping("/me")
    public ResponseEntity<?> getLoggedInUserDetails(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(userDetails);
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        // Authenticating the user details from the payload LoginRequest with username
        // and password
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String jwt = jwtUtils.generateJwtToken(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())// can be replaced with GrantAuthority::getAuthority
                .collect(Collectors.toList());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        return ResponseEntity.ok(new JwtResponse(
                jwt,
                refreshToken.getToken(),
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getFirstName(),
                userDetails.getLastName(),
                userDetails.getEmail(),
                roles,
                userDetails.getProfileImageUrl()));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtUtils.generateTokenFromUsername(user.getUsername());
                    return ResponseEntity.ok(new TokenRefreshResponse(token, requestRefreshToken));
                })
                .orElseThrow(
                        () -> new TokenRefreshException(requestRefreshToken, "Refresh Token is not in the database."));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegistrationRequest registrationRequest) {
        if (userRepository.existsByUsername(registrationRequest.getUsername())) {
            throw new UsernameAlreadyExistsException("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(registrationRequest.getEmail())) {
            throw new EmailAlreadyExistsException("Error: Email already in use!");
        }

        User user = new User(
                registrationRequest.getUsername(),
                registrationRequest.getEmail(),
                registrationRequest.getFirstName(),
                registrationRequest.getLastName(),
                encoder.encode(registrationRequest.getPassword()),
                "https://d3cdw8ymz2nt7l.cloudfront.net/profileImages/default_avatar.jpg");

        Set<String> strRoles = registrationRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role not found!"));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role not found!"));
                        roles.add(adminRole);

                        break;
                    case "mod":
                        Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                                .orElseThrow(() -> new RuntimeException("Error: Role not found!"));
                        roles.add(modRole);

                        break;
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role not found!"));
                        roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        MessageResponse response = new MessageResponse(
                HttpStatus.CREATED.value(),
                "User registered successfully!");

        return ResponseEntity.ok(response);
    }
}
