package org.thoughtlabs.blogbackend.services;

import jakarta.mail.MessagingException;
import org.thoughtlabs.blogbackend.exceptions.EmailFailureException;
import org.thoughtlabs.blogbackend.exceptions.UserNotVerifiedException;
import org.thoughtlabs.blogbackend.models.EPostStatus;
import org.thoughtlabs.blogbackend.models.Post;
import org.thoughtlabs.blogbackend.models.User;
import org.thoughtlabs.blogbackend.models.VerificationToken;
import org.thoughtlabs.blogbackend.payload.request.LoginRequest;
import org.thoughtlabs.blogbackend.payload.request.PasswordResetBody;
import org.thoughtlabs.blogbackend.payload.request.RegistrationRequest;
import org.thoughtlabs.blogbackend.payload.request.UserUpdateRequest;
import org.thoughtlabs.blogbackend.payload.response.JwtResponse;

import java.util.List;
import java.util.Map;

public interface UserService {

    public List<User> getAllUsersByRole(String roleName);

    public List<Post> getUsersPostByStatus(Long id, EPostStatus status);

//    public List<Post> getPostsByUserId(Long id);

    public boolean verifyEmail(String token);

    public void forgotPassword(String email) throws EmailFailureException;

    public boolean resetPassword(PasswordResetBody passwordResetBody);

    public User registerUser(RegistrationRequest registrationRequest) throws MessagingException, EmailFailureException;

    public JwtResponse loginUser(LoginRequest loginRequest) throws MessagingException, EmailFailureException, UserNotVerifiedException;

    public User updateUserProfile(Long id, UserUpdateRequest userUpdateRequest);

    public User patchUserProfile(Long id, Map<String, Object> update);

    public String deleteUserAccount(Long id);

    public VerificationToken createVerificationToken(User user);

//    public User createOrUpdateOAuth2User(String username, String email, String firstName, String lastName, String profileImageUrl, String provider);
}
