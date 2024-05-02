package org.example.emsbackend.services;

import jakarta.transaction.Transactional;
import org.example.emsbackend.models.User;
import org.example.emsbackend.payload.request.UserUpdateRequest;

import java.util.List;
import java.util.Optional;

public interface UserService {

    public List<User> getAllUsersByRole(String roleName);

    public User findByUsername(String username);

    public User updateUserProfile(Long id, UserUpdateRequest userUpdateRequest);

    public String deleteUserAccount(Long id);

}
