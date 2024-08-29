package org.thoughtlabs.blogbackend.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.thoughtlabs.blogbackend.services.UserServiceImpl;

import java.util.Collections;
import java.util.Map;

@RestController
@Slf4j
public class OAuth2Controller {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private OAuth2AuthorizedClientService clientService;

    @GetMapping("/oauth2-user")
    public ResponseEntity<String> handleOAuthCallback(@AuthenticationPrincipal OAuth2AuthenticationToken authToken) {
        OAuth2AuthorizedClient client = clientService.loadAuthorizedClient(
                authToken.getAuthorizedClientRegistrationId(),
                authToken.getName()
        );
//        log.info("Received OAuth2 Callback for provider : {} with parameters: {}");
        log.info("OAUTH2USER VIA TOKEN::::::::> {}", client);
        String accessToken = client.getAccessToken().getTokenValue();
        return ResponseEntity.ok(accessToken);
    }
}
