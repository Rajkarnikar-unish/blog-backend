package org.thoughtlabs.blogbackend.security.services.oauth2;

import io.micrometer.common.util.StringUtils;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.thoughtlabs.blogbackend.exceptions.EmailFailureException;
import org.thoughtlabs.blogbackend.models.*;
import org.thoughtlabs.blogbackend.repositories.RoleRepository;
import org.thoughtlabs.blogbackend.repositories.UserRepository;
import org.thoughtlabs.blogbackend.security.exception.OAuth2AuthenticationProcessingException;
import org.thoughtlabs.blogbackend.security.jwt.JwtUtils;
import org.thoughtlabs.blogbackend.security.services.UserDetailsImpl;
import org.thoughtlabs.blogbackend.security.services.oauth2.user.OAuth2UserInfo;
import org.thoughtlabs.blogbackend.security.services.oauth2.user.OAuth2UserInfoFactory;
import org.thoughtlabs.blogbackend.services.EmailService;
import org.thoughtlabs.blogbackend.services.UserService;
import org.thoughtlabs.blogbackend.services.UserServiceImpl;

import javax.naming.AuthenticationException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class OAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) throws MessagingException, EmailFailureException {
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(oAuth2UserRequest.getClientRegistration().getRegistrationId(), oAuth2User.getAttributes());
        if(StringUtils.isEmpty(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider!");
        }

        Optional<User> userOptional = userRepository.findByEmail(oAuth2UserInfo.getEmail());
        User user;
        if(userOptional.isPresent()) {
            user = userOptional.get();
            if(user.getProvider() == null ||
                    !user.getProvider().equals(AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()))){
                throw new OAuth2AuthenticationProcessingException("Looks like you're signed up with "
                        + user.getProvider() + " account. Please use your "
                        + user.getProvider() + " account to login.");
            }

            user = updateExistingUser(user, oAuth2UserInfo);
        } else {
            user = registerNewUser(oAuth2UserRequest, oAuth2UserInfo);
        }

        return UserDetailsImpl.build(user, oAuth2User.getAttributes());
    }

    private User registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) throws MessagingException, EmailFailureException {
        String name = oAuth2UserInfo.getName();
        String[] nameSep = name != null ? name.split(" ") : new String[0];

        User user = new User(
                name,
                oAuth2UserInfo.getEmail(),
                nameSep[0],
                nameSep.length == 2 ? nameSep[1]: "N/A",
                encoder.encode("OAuth2" + nameSep[0] + "PW"),
                oAuth2UserInfo.getImageUrl(),
                roleRepository.findByName(ERole.ROLE_USER).orElseThrow(() -> new RuntimeException("Error: Role not found!")),
                oAuth2UserInfo.getId(),
                AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId())
        );

        VerificationToken verificationToken = userService.createVerificationToken(user);
        emailService.sendEmailVerificationEmail(verificationToken);

        return userRepository.save(user);
    }

    private User updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo) {
        String name = oAuth2UserInfo.getName();
        String[] nameSep = name != null ? name.split(" ") : new String[0];
        existingUser.setFirstName(nameSep[0]);
        existingUser.setLastName(nameSep.length==2? nameSep[1]: "N/A");
        existingUser.setProfileImageUrl(oAuth2UserInfo.getImageUrl());
        return userRepository.save(existingUser);
    }
}
