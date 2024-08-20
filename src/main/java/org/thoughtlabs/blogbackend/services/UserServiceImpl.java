package org.thoughtlabs.blogbackend.services;

import jakarta.transaction.Transactional;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.thoughtlabs.blogbackend.exceptions.EmailNotFoundException;
import org.thoughtlabs.blogbackend.models.Post;
import org.thoughtlabs.blogbackend.models.User;
import org.thoughtlabs.blogbackend.payload.request.UserUpdateRequest;
import org.thoughtlabs.blogbackend.repositories.PostRepository;
import org.thoughtlabs.blogbackend.repositories.RefreshTokenRepository;
import org.thoughtlabs.blogbackend.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    UserRepository userRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @Override
    public List<User> getAllUsersByRole(String roleName) {
        return userRepository.getAllUsersByRole(roleName).orElseThrow(() -> new UsernameNotFoundException("User with role ROLE_USER not found"));
    }

    public List<Post> getPostsByUserId(Long userId) {
        Optional<List<Post>> opPosts = postRepository.getPostsByUserId(userId);
        if(opPosts.isPresent()) {
            return opPosts.get();
        }
        return new ArrayList<>();
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User with "+ username +" not found"));
    }

    @Override
    public User updateUserProfile(Long id, UserUpdateRequest userUpdateRequest) {
        Optional<User> opUser = userRepository.findById(id);
        if (opUser.isPresent()) {
            User user = opUser.get();
            user.setUsername(userUpdateRequest.getUsername());
            user.setFirstName(userUpdateRequest.getFirstName());
            user.setLastName(userUpdateRequest.getLastName());
            user.setEmail(userUpdateRequest.getEmail());

            return userRepository.save(user);

        }
        throw new UsernameNotFoundException("User with username: " + userUpdateRequest.getUsername() + " not found!");
    }

    @Override
    public User patchUserProfile(Long id, Map<String, Object> updates) {
        Optional<User> opUser = userRepository.findById(id);

        if(opUser.isPresent()){
            User user = opUser.get();
            updates.forEach((k, v) ->{
                Field field = ReflectionUtils.findField(User.class, k);
                field.setAccessible(true);
                ReflectionUtils.setField(field, user, v);
            });
            return userRepository.save(user);
        }
        throw new UsernameNotFoundException("Username not found");
    }

    public User getLoggedInUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Override
    @Transactional
    public String deleteUserAccount(Long id) {
        Optional<User> opUser = userRepository.findById(id);
        User currentUser = getLoggedInUser();
        if(opUser.isPresent() && currentUser.getId() == id) {
            refreshTokenRepository.deleteByUser(opUser.get());
            userRepository.deleteById(id);
            return "Your account has been deleted successfully.";
        }
        return "Not authorized to delete this account!";
    }

    @Override
    public void processOAuthPostLogin(OAuth2User oAuth2User, String provider) {
        logger.info("PROVIDER ----> {}", provider);
        switch (provider) {
            //TODO: SAVE USER ACCORDING TO PROVIDER
            case "google":
                Map<String, Object> attributes = oAuth2User.getAttributes();
                logger.info("ATTRIBUTES------> {}", attributes);
                break;
            case "github":
                String email = oAuth2User.getAttribute("email");
                String username = oAuth2User.getAttribute("login");
                String profileImageUrl = oAuth2User.getAttribute("avatar_url");
                String fullName = oAuth2User.getAttribute("name");
                String[] name = fullName != null ? fullName.split(" ") : new String[0];
                String firstName = name[0];
                String lastName = name[1];

                Optional<User> existingUser = userRepository.findByEmail(email);
                if (existingUser.isEmpty()) {
                    User providerUser = User.builder()
                            .username(username)
                            .email(email)
                            .firstName(firstName)
                            .lastName(lastName)
                            .profileImageUrl(profileImageUrl)
                            .password("OAUTH_DEFAULT_PASSWORD")
                            .providerName(provider)
                            .build();

                    userRepository.save(providerUser);
                }
                break;
//            case "facebook":
//                break;
            default:
                break;
        }
//        User existingUser = userRepository.findByEmail(email).orElseThrow(() -> new EmailNotFoundException("Email Address was not found!"));

//        logger.info("OAUTH2 USER =======>{}", attributes);
//        logger.info("EXISTING USER =======>{}", existingUser);
    }

}
