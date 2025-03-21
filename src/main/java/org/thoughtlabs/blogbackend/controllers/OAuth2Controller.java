package org.thoughtlabs.blogbackend.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.web.bind.annotation.*;
import org.thoughtlabs.blogbackend.security.services.UserDetailsImpl;
import org.thoughtlabs.blogbackend.services.UserServiceImpl;
import org.thoughtlabs.blogbackend.util.CookieUtils;

@RestController
@Slf4j
@RequestMapping("/api/auth")
public class OAuth2Controller {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private OAuth2AuthorizedClientService clientService;

    @GetMapping("/oauth2-user")
    public ResponseEntity<?> getOAuth2User(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        if(userDetailsImpl == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated!");
        }
        return ResponseEntity.ok(userDetailsImpl);
    }

    @PostMapping("/oauth2-logout")
    public ResponseEntity<?> logoutOAuth2User(HttpServletRequest request, HttpServletResponse response) {
        CookieUtils.deleteCookie(request, response, "access_token");
        CookieUtils.deleteCookie(request, response, "refresh_token");
        return ResponseEntity.ok().body("User logged out successfully");
    }
}