package org.example.blogbackend.services;

import jakarta.transaction.Transactional;
import org.example.blogbackend.models.Post;
import org.example.blogbackend.models.User;
import org.example.blogbackend.payload.request.UserUpdateRequest;
import org.example.blogbackend.repositories.PostRepository;
import org.example.blogbackend.repositories.RefreshTokenRepository;
import org.example.blogbackend.repositories.UserRepository;
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
public class UserServiceImpl implements UserService{

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

}
