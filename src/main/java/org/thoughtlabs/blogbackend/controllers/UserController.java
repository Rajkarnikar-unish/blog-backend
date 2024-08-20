package org.thoughtlabs.blogbackend.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.thoughtlabs.blogbackend.exceptions.FileUploadFailureException;
import org.thoughtlabs.blogbackend.exceptions.PostsNotFoundException;
import org.thoughtlabs.blogbackend.models.Post;
import org.thoughtlabs.blogbackend.models.User;
import org.thoughtlabs.blogbackend.payload.request.UserUpdateRequest;
import org.thoughtlabs.blogbackend.payload.response.MessageResponse;
import org.thoughtlabs.blogbackend.repositories.UserRepository;
import org.thoughtlabs.blogbackend.services.PostService;
import org.thoughtlabs.blogbackend.services.StorageService;
import org.thoughtlabs.blogbackend.services.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@Slf4j
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PostService postService;

    @Autowired
    UserServiceImpl userService;
    // UserServiceImpl userService = new UserServiceImpl();

    @Autowired
    private StorageService storageService;

    @Value("${cloudfront.url}")
    private String cloudFrontUrl;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/user-role")
    public ResponseEntity<?> getAllUsersByRole(@RequestParam String roleName) {
        return ResponseEntity.ok(userService.getAllUsersByRole(roleName));
    }

    @GetMapping("/{id}/posts")
    public ResponseEntity<List<Post>> getAllPostsByUser(@PathVariable Long id) throws PostsNotFoundException {
        return ResponseEntity.ok(userService.getPostsByUserId(id));
    }

    @PostMapping("/upload-profile-image")
    public ResponseEntity<MessageResponse> uploadProfileImage(@RequestParam(value = "profile_image") MultipartFile file,
            @RequestParam("userId") Long userId) {
        try {
            String fileName = storageService.uploadFile(file);
            String imageUrl = cloudFrontUrl + "/profileImages/" + fileName;

            User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
            user.setProfileImageUrl(imageUrl);
            userRepository.save(user);

            MessageResponse messageResponse = new MessageResponse(
                    HttpStatus.OK.value(),
                    "Image Uploaded Successfully.");

            return ResponseEntity.ok(messageResponse);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new FileUploadFailureException("File upload failed");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUserProfile(@PathVariable Long id, @RequestBody UserUpdateRequest updateRequest) {
        User user = userService.updateUserProfile(id, updateRequest);
        return ResponseEntity.ok(user);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<User> patchUserProfile(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        User user = userService.patchUserProfile(id, updates);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUserProfile(@PathVariable("id") Long id) {
        return ResponseEntity.ok(userService.deleteUserAccount(id));
    }

}
