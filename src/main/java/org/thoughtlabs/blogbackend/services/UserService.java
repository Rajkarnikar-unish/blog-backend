package org.thoughtlabs.blogbackend.services;

import jakarta.mail.MessagingException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.thoughtlabs.blogbackend.exceptions.EmailFailureException;
import org.thoughtlabs.blogbackend.models.EPostStatus;
import org.thoughtlabs.blogbackend.models.Post;
import org.thoughtlabs.blogbackend.models.User;
import org.thoughtlabs.blogbackend.payload.request.RegistrationRequest;
import org.thoughtlabs.blogbackend.payload.request.UserUpdateRequest;

import java.util.List;
import java.util.Map;

public interface UserService {

    public List<User> getAllUsersByRole(String roleName);

    public List<Post> getUsersPostByStatus(Long id, EPostStatus status);

//    public List<Post> getPostsByUserId(Long id);

    public User registerUser(RegistrationRequest registrationRequest) throws MessagingException, EmailFailureException;

    public User updateUserProfile(Long id, UserUpdateRequest userUpdateRequest);

    public User patchUserProfile(Long id, Map<String, Object> update);

    public String deleteUserAccount(Long id);

    public User createOrUpdateOAuth2User(String username, String email, String firstName, String lastName, String profileImageUrl, String provider);
}
