package org.thoughtlabs.blogbackend.controllers;

import com.amazonaws.services.memorydb.model.UserAlreadyExistsException;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.thoughtlabs.blogbackend.exceptions.EmailFailureException;
import org.thoughtlabs.blogbackend.exceptions.EmailNotFoundException;
import org.thoughtlabs.blogbackend.exceptions.UserNotVerifiedException;
import org.thoughtlabs.blogbackend.models.RefreshToken;
import org.thoughtlabs.blogbackend.payload.request.LoginRequest;
import org.thoughtlabs.blogbackend.payload.request.PasswordResetBody;
import org.thoughtlabs.blogbackend.payload.request.RegistrationRequest;
import org.thoughtlabs.blogbackend.payload.request.TokenRefreshRequest;
import org.thoughtlabs.blogbackend.payload.response.JwtResponse;
import org.thoughtlabs.blogbackend.payload.response.MessageResponse;
import org.thoughtlabs.blogbackend.payload.response.TokenRefreshResponse;
import org.thoughtlabs.blogbackend.repositories.RoleRepository;
import org.thoughtlabs.blogbackend.repositories.UserRepository;
import org.thoughtlabs.blogbackend.security.exception.TokenRefreshException;
import org.thoughtlabs.blogbackend.security.jwt.JwtUtils;
import org.thoughtlabs.blogbackend.security.services.RefreshTokenService;
import org.thoughtlabs.blogbackend.security.services.UserDetailsImpl;
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
import org.thoughtlabs.blogbackend.services.UserServiceImpl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    private UserServiceImpl userService;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @GetMapping("/me")
    public ResponseEntity<?> getLoggedInUserDetails(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(userDetails);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest) throws UserNotVerifiedException, MessagingException, EmailFailureException {
        JwtResponse jwtResponse = userService.loginUser(loginRequest);
        jwtResponse.setStatus(HttpStatus.OK.value());
        return ResponseEntity.ok(jwtResponse);
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
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegistrationRequest registrationRequest) throws MessagingException {
        try {
            userService.registerUser(registrationRequest);

            MessageResponse response = new MessageResponse(
                    HttpStatus.CREATED.value(),
                    "User registered successfully!");

            return ResponseEntity.ok(response);

        } catch (UserAlreadyExistsException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (EmailFailureException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        if(userService.verifyEmail(token)) {
            MessageResponse response = new MessageResponse(
                    HttpStatus.OK.value(),
                    "You have successfully verified you email."
            );
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        try {
             userService.forgotPassword(email);
            MessageResponse response = new MessageResponse(
                    HttpStatus.OK.value(),
                    "Please check your email for updating your password."
            );
            return ResponseEntity.ok(response);
        } catch (EmailFailureException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception ex) {
            throw new EmailNotFoundException("Email address was not found!");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordResetBody passwordResetBody) {
        if(userService.resetPassword(passwordResetBody)) {
            MessageResponse response = new MessageResponse(
                    HttpStatus.OK.value(),
                    "Password reset successful."
            );
            return ResponseEntity.ok(response);
        }else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
