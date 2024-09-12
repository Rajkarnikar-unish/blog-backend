package org.thoughtlabs.blogbackend.services;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.thoughtlabs.blogbackend.models.User;
import org.thoughtlabs.blogbackend.security.jwt.JwtUtils;

import java.util.Map;

@Slf4j
@Service
public class OAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        User user;
        switch (registrationId) {
            case "github":
                String githubUsername = oAuth2User.getAttribute("login");
                String githubEmail = oAuth2User.getAttribute("email");
                String githubProfileImg = oAuth2User.getAttribute("avatar_url");
                String githubFullName = oAuth2User.getAttribute("name");
                String[] githubName = githubFullName != null ? githubFullName.split(" ") : new String[0];
                String githubFirstName = githubName[0];
                String githubLastName = githubName[1];

                user = userService.createOrUpdateOAuth2User(githubUsername, githubEmail, githubFirstName, githubLastName, githubProfileImg, registrationId );
                break;

            case "facebook":
                String facebookUsername = oAuth2User.getAttribute("name");
                String facebookEmail = oAuth2User.getAttribute("email");
                String[] facebookFullname = facebookUsername != null ? facebookUsername.split(" ") : new String[0];
                String facebookFirstName = facebookFullname[0];
                String facebookLastName = facebookFullname[1];

                user = userService.createOrUpdateOAuth2User(facebookUsername, facebookEmail, facebookFirstName, facebookLastName, "https://d3cdw8ymz2nt7l.cloudfront.net/profileImages/default_avatar.jpg", registrationId);
                break;

            case "google":
                String googleEmail = oAuth2User.getAttribute("email");
                String googleUsername = oAuth2User.getAttribute("given_name");
                String googleProfileImg = oAuth2User.getAttribute("picture");
                String fullName = oAuth2User.getAttribute("name");
                String[] googleName = fullName != null ? fullName.split(" ") : new String[0];
                String googleFirstName= googleName[0];
                String googleLastName= googleName.length == 2 ? googleName[1] : "N/A";

                user = userService.createOrUpdateOAuth2User(googleUsername, googleEmail, googleFirstName, googleLastName, googleProfileImg, registrationId);
                break;

            default:
                log.warn("Unsupported registration id {}", registrationId);
                throw new OAuth2AuthenticationException("Unsupported Registration ID " + registrationId);
        }

        return new DefaultOAuth2User(oAuth2User.getAuthorities(), oAuth2User.getAttributes(), "name");
    }
}
