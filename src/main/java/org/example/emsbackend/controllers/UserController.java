package org.example.emsbackend.controllers;

import org.example.emsbackend.models.User;
import org.example.emsbackend.payload.request.UserUpdateRequest;
import org.example.emsbackend.repositories.UserRepository;
import org.example.emsbackend.services.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserServiceImpl userService;
//    UserServiceImpl userService = new UserServiceImpl();

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/user-role")
    public ResponseEntity<?> getAllUsersByRole(@RequestParam String roleName) {
        return ResponseEntity.ok(userService.getAllUsersByRole(roleName));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<User> updateUserProfile(@PathVariable Long id, @RequestBody UserUpdateRequest updateRequest) {
        User user = userService.updateUserProfile(id, updateRequest);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUserProfile(@PathVariable("id") Long id) {
        return ResponseEntity.ok(userService.deleteUserAccount(id));
    }

}
