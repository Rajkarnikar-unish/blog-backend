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
        String email = oAuth2User.getAttribute("email");
        Optional<User> existingUser = userRepository.findByEmail(email);
        //TODO: SAVE USER ACCORDING TO PROVIDER
        switch (provider) {
            case "google":
                Map<String, Object> attributes = oAuth2User.getAttributes();
                logger.info("ATTRIBUTES------> {}", attributes);
                break;
            case "github":
                String githubUsername = oAuth2User.getAttribute("login");
                String githubProfileImageUrl = oAuth2User.getAttribute("avatar_url");
                String githubFullName = oAuth2User.getAttribute("name");
                String[] name = githubFullName != null ? githubFullName.split(" ") : new String[0];
                String githubFirstName = name[0];
                String githubLastName = name[1];

                if (existingUser.isEmpty()) {
                    User providerUser = User.builder()
                            .username(githubUsername)
                            .email(email)
                            .firstName(githubFirstName)
                            .lastName(githubLastName)
                            .profileImageUrl(githubProfileImageUrl)
                            .password("OAUTH_DEFAULT_PASSWORD")
                            .providerName(provider)
                            .build();

                    userRepository.save(providerUser);
                } else {
                    //TODO: CASE FOR IF USER ALREADY EXISTS
                }
                break;
            case "facebook":
                String facebookUsername = oAuth2User.getAttribute("name");
                String[] facebookFullName = facebookUsername != null ? facebookUsername.split(" ") : new String[0];
                String facebookFirstName = facebookFullName[0];
                String facebookLastName = facebookFullName[1];

                existingUser = userRepository.findByEmail(email);
                if (existingUser.isEmpty()) {
                    User providerUser = User.builder()
                            .username(facebookUsername)
                            .firstName(facebookFirstName)
                            .lastName(facebookLastName)
                            .email(email)
                            .password("OAUTH_DEFAULT_PASSWORD")
                            .providerName(provider)
                            .build();

                    userRepository.save(providerUser);
                } else {
                    User userToUpdate = existingUser.get();
                    boolean isUpdated = false;

                    if(!facebookUsername.equals(userToUpdate.getUsername())) {
                        userToUpdate.setUsername(facebookUsername);
                        isUpdated = true;
                    }
                    if(!facebookFirstName.equals(userToUpdate.getFirstName())) {
                        userToUpdate.setFirstName(facebookFirstName);
                        isUpdated = true;
                    }
                    if(!facebookLastName.equals(userToUpdate.getLastName())) {
                        userToUpdate.setLastName(facebookLastName);
                        isUpdated = true;
                    }
                    if(userToUpdate.getProviderName() == null || !provider.equals(userToUpdate.getProviderName())) {
                        userToUpdate.setProviderName(provider);
                        isUpdated = true;
                    }

                    if (isUpdated) userRepository.save(userToUpdate);
                }
                break;
            default:
                break;
        }
    }

}
