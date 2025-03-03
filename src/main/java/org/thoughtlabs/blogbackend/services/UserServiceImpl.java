package org.thoughtlabs.blogbackend.services;

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.thoughtlabs.blogbackend.exceptions.EmailAlreadyExistsException;
import org.thoughtlabs.blogbackend.exceptions.EmailFailureException;
import org.thoughtlabs.blogbackend.exceptions.UsernameAlreadyExistsException;
import org.thoughtlabs.blogbackend.models.*;
import org.thoughtlabs.blogbackend.payload.request.RegistrationRequest;
import org.thoughtlabs.blogbackend.payload.request.UserUpdateRequest;
import org.thoughtlabs.blogbackend.repositories.PostRepository;
import org.thoughtlabs.blogbackend.repositories.RefreshTokenRepository;
import org.thoughtlabs.blogbackend.repositories.RoleRepository;
import org.thoughtlabs.blogbackend.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;
import org.thoughtlabs.blogbackend.security.jwt.JwtUtils;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @Autowired
    EmailService emailService;

    @Autowired
    PasswordEncoder encoder;

    @Override
    public List<User> getAllUsersByRole(String roleName) {
        return userRepository.getAllUsersByRole(roleName)
                .orElseThrow(() -> new UsernameNotFoundException("User with role " + roleName + " not found"));
    }

//    public List<Post> getPostsByUserId(Long userId) {
//        return postRepository.getPostsByUserId(userId).orElseGet(ArrayList::new);
//    }

    @Override
    public List<Post> getUsersPostByStatus(Long userId, EPostStatus status) {
        String statusStr = status.name();
        List<Post> posts = postRepository.getUserPostsBasedOnStatus(userId, statusStr);
        if (!posts.isEmpty()) {
            return posts;
        }
        return new ArrayList<>();
    }

    @Override
    public User registerUser(RegistrationRequest registrationRequest) throws UsernameAlreadyExistsException, EmailAlreadyExistsException, MessagingException, EmailFailureException {
        if(userRepository.existsByUsername(registrationRequest.getUsername())) {
            throw new UsernameAlreadyExistsException("Error: Username is already taken!");
        }

        if(userRepository.existsByEmail(registrationRequest.getEmail())) {
            throw new EmailAlreadyExistsException("Error: Email address already in use!");
        }

        User user = new User();
        user.setEmail(registrationRequest.getEmail());
        user.setUsername(registrationRequest.getUsername());
        user.setFirstName(registrationRequest.getFirstName());
        user.setLastName(registrationRequest.getLastName());
        user.setPassword(encoder.encode(registrationRequest.getPassword()));

        VerificationToken verificationToken = createVerificationToken(user);
        emailService.sendEmailVerificationEmail(verificationToken);

        Set<String> strRoles = registrationRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if(strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role not found!"));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch(role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role not found!"));
                        roles.add(adminRole);
                        break;
                    case "mod":
                        Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                                .orElseThrow(() -> new RuntimeException("Error: Role not found!"));
                        roles.add(modRole);
                        break;
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role not found!"));
                        roles.add(userRole);
                        break;
                }
            });
        }

        user.setRoles(roles);
        return userRepository.save(user);
    }

    @Override
    public User updateUserProfile(Long id, UserUpdateRequest userUpdateRequest) {
        return userRepository.findById(id).map(user -> {
            user.setUsername(userUpdateRequest.getUsername());
            user.setFirstName(userUpdateRequest.getFirstName());
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

    public VerificationToken createVerificationToken(User user) {
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(jwtUtils.generateEmailVerificationToken(user.getEmail()));
        verificationToken.setCreatedAt(LocalDateTime.now());
        verificationToken.setUser(user);
        user.getVerificationTokens().add(verificationToken);

        return verificationToken;
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
//            if(existingUser.getProviderName() == null || !provider.equals(existingUser.getProviderName())) {
//                existingUser.setProviderName(provider);
//                isUpdated = true;
//            }
            if(isUpdated) userRepository.save(existingUser);
            return existingUser;
        }).orElseGet(() -> {
            User user = User.builder()
                    .username(username)
                    .email(email)
                    .firstName(firstName)
                    .lastName(lastName)
                    .profileImageUrl(profileImageUrl)
//                    .providerName(provider)
                    .password("OAUTH_DEFAULT_PASSWORD")
                    .build();
            return userRepository.save(user);
        });
    }
}
