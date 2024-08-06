package org.thoughtlabs.blogbackend.controllers;

import lombok.extern.slf4j.Slf4j;
import org.thoughtlabs.blogbackend.models.User;
import org.thoughtlabs.blogbackend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@Slf4j
@RequestMapping("/api")
public class OAuth2Controller {

    private final OAuth2AuthorizedClientService clientService;

    @Autowired
    private OAuth2Controller(OAuth2AuthorizedClientService clientService) {
        this.clientService = clientService;
    }

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/login/oauth2/code/{provider}")
    public RedirectView loginSuccess(@PathVariable String provider, Authentication authentication) {
        if(authentication instanceof OAuth2AuthenticationToken authenticationToken) {

            OAuth2User oAuth2User = authenticationToken.getPrincipal();

            String userEmail = (String) oAuth2User.getAttributes().get("email");
            String firstName = (String) oAuth2User.getAttributes().get("given_name");
            String lastName= (String) oAuth2User.getAttributes().get("family_name");
            String username = (String) oAuth2User.getAttributes().get("name");
            String profileImageUrl = (String) oAuth2User.getAttributes().get("picture");

            Boolean existingUser = userRepository.existsByEmail(userEmail);
            System.out.println(existingUser);
            if(!existingUser) {
                User user = new User();
                user.setEmail(userEmail);
                user.setUsername(username);
                user.setFirstName(firstName);
                user.setLastName(lastName);
                user.setProfileImageUrl(profileImageUrl);
                user.setProviderName(provider);
                user.setPassword("");

                userRepository.save(user);
            }

            log.info("LOGGED IN USER:::::::>", existingUser );

        }

        return new RedirectView("/login-success");
    }
}
