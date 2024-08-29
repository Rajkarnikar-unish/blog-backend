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
        return userRepository.getAllUsersByRole(roleName)
                .orElseThrow(() -> new UsernameNotFoundException("User with role " + roleName + " not found"));
    }

    public List<Post> getPostsByUserId(Long userId) {
        return postRepository.getPostsByUserId(userId).orElseGet(ArrayList::new);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User with "+ username +" not found"));
    }

    @Override
    public User updateUserProfile(Long id, UserUpdateRequest userUpdateRequest) {
//        Optional<User> opUser = userRepository.findById(id);
//        if (opUser.isPresent()) {
//            User user = opUser.get();
//            user.setUsername(userUpdateRequest.getUsername());
//            user.setFirstName(userUpdateRequest.getFirstName());
//            user.setLastName(userUpdateRequest.getLastName());
//            user.setEmail(userUpdateRequest.getEmail());
//
//            return userRepository.save(user);
//
//        }
//        throw new UsernameNotFoundException("User with username: " + userUpdateRequest.getUsername() + " not found!");
        return userRepository.findById(id).map(user -> {
            user.setUsername(userUpdateRequest.getFirstName());
            user.setFirstName(userUpdateRequest.getLastName());
            user.setLastName(userUpdateRequest.getLastName());
            user.setEmail(userUpdateRequest.getEmail());
            return userRepository.save(user);
        }).orElseThrow(() -> new UsernameNotFoundException("User with username: " + userUpdateRequest.getUsername() + " not found!"));
    }

    @Override
    public User patchUserProfile(Long id, Map<String, Object> updates) {
        Optional<User> opUser = userRepository.findById(id);

        if(opUser.isPresent()){
            User user = opUser.get();
            updates.forEach((k, v) ->{
                Field field = ReflectionUtils.findField(User.class, k);
                if(field != null){
                    field.setAccessible(true);
                    ReflectionUtils.setField(field, user, v);
                }
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
        if(opUser.isPresent() && currentUser.getId().equals(id)) {
            refreshTokenRepository.deleteByUser(opUser.get());
            userRepository.deleteById(id);
            return "Your account has been deleted successfully.";
        }
        return "Not authorized to delete this account!";
    }

    @Override
    public User createOrUpdateOAuth2User(String username, String email, String firstName, String lastName, String profileImageUrl, String provider) {
        return userRepository.findByEmail(email).map(existingUser -> {
            boolean isUpdated = false;
            if (!username.equals(existingUser.getUsername())) {
                existingUser.setUsername(username);
                isUpdated = true;
            }
            if(!email.equals(existingUser.getEmail())) {
                existingUser.setEmail(email);
                isUpdated = true;
            }
            if(!firstName.equals(existingUser.getFirstName())) {
                existingUser.setFirstName(firstName);
                isUpdated = true;
            }
            if(!lastName.equals(existingUser.getLastName())) {
                existingUser.setLastName(lastName);
                isUpdated = true;
            }
            if(existingUser.getProviderName() == null || !provider.equals(existingUser.getProviderName())) {
                existingUser.setProviderName(provider);
                isUpdated = true;
            }
            if(isUpdated) userRepository.save(existingUser);
            return existingUser;
        }).orElseGet(() -> {
            User user = User.builder()
                    .username(username)
                    .email(email)
                    .firstName(firstName)
                    .lastName(lastName)
                    .profileImageUrl(profileImageUrl)
                    .providerName(provider)
                    .password("OAUTH_DEFAULT_PASSWORD")
                    .build();
            return userRepository.save(user);
        });
    }
}
