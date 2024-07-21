package org.example.emsbackend.controllers;

import org.example.emsbackend.models.User;
import org.example.emsbackend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Optional;

@RestController
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
            if(!existingUser) {
                User user = new User();
                user.setEmail(userEmail);
                user.setUsername(username);
                user.setFirstName(firstName);
                user.setLastName(lastName);
                user.setProfileImageUrl(profileImageUrl);
                user.setPassword("");

                userRepository.save(user);
            }

        }

        return new RedirectView("/login-success");
    }
}
