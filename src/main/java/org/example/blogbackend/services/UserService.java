package org.example.blogbackend.services;

import org.example.blogbackend.models.Post;
import org.example.blogbackend.models.User;
import org.example.blogbackend.payload.request.UserUpdateRequest;

import java.util.List;

public interface UserService {

    public List<User> getAllUsersByRole(String roleName);

    public List<Post> getPostsByUserId(Long id);

    public User findByUsername(String username);

    public User updateUserProfile(Long id, UserUpdateRequest userUpdateRequest);

    public String deleteUserAccount(Long id);
}
