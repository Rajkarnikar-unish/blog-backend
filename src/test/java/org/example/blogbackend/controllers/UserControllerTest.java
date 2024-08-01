package org.example.blogbackend.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.With;
import org.example.blogbackend.models.EPostStatus;
import org.example.blogbackend.models.Post;
import org.example.blogbackend.models.User;
import org.example.blogbackend.payload.request.UserUpdateRequest;
import org.example.blogbackend.repositories.UserRepository;
import org.example.blogbackend.services.PostService;
import org.example.blogbackend.services.StorageService;
import org.example.blogbackend.services.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.util.*;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserServiceImpl userServiceImpl;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PostService postService;

    @MockBean
    private StorageService storageService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${cloudfront.url}")
    private String cloudFront;

    User user;

    @BeforeEach
    void setup() {
        user = User.builder()
                .id(1L)
                .firstName("FirstNameA")
                .lastName("LastNameA")
                .email("emailA@junit.com")
                .username("TestAUsername")
                .password("PasswordA123")
                .build();
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldReturnListOfUsersWithGivenRole() throws Exception {
        List<User> usersList = new ArrayList<>();
        usersList.add(user);
        User user2 = User.builder()
                .id(2L)
                .username("TestBUsername")
                .firstName("FirstNameB")
                .lastName("LastNameB")
                .email("emailB@junit.com")
                .password("PasswordB123")
                .build();
        usersList.add(user2);
        given(userServiceImpl.getAllUsersByRole(anyString())).willReturn(usersList);

        ResultActions response = mockMvc.perform(get("/api/users/user-role")
                .param("roleName", "someRole"));

        response.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(usersList.size())));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldReturnSuccessMessageOnUploadProfileImageWithGivenId() throws Exception{
        String fileName = "testProfile.jpg";

        MockMultipartFile mockFile = new MockMultipartFile("profile_image", fileName, MediaType.IMAGE_JPEG_VALUE, "test image content".getBytes());

        given(storageService.uploadFile(any(MultipartFile.class))).willReturn(fileName);
        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

        ResultActions response = mockMvc.perform(multipart("/api/users/upload-profile-image")
                .file(mockFile)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .param("userId", user.getId().toString())
                .with(SecurityMockMvcRequestPostProcessors.csrf()));

        response.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.message", is("Image Uploaded Successfully.")));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldReturnFailureMessageOnUploadProfileImageWithGivenId() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile("profile_image", "testProfile.jpg", MediaType.IMAGE_JPEG_VALUE, "test iamge content".getBytes());

        given(userRepository.findById(2L)).willReturn(Optional.empty());

        ResultActions response = mockMvc.perform(multipart("/api/users/upload-profile-image")
                .file(mockFile)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .param("userId", "2")
                .with(SecurityMockMvcRequestPostProcessors.csrf()));

        response.andExpect(status().isConflict())
                .andDo(print())
                .andExpect(jsonPath("$.message", is("File upload failed")));
    }

//    @Test
//    @WithMockUser(username = "user", roles = {"USER"})
//    void shouldUpdateUserProfileSuccessfullyWithGivenId() throws Exception {
//        UserUpdateRequest request = UserUpdateRequest
//                .builder()
//                .email("updatedEmail@junit.com")
//                .username("TestUpdatedUserA")
//                .firstName("UpdatedFirstName")
//                .lastName("UpdatedLastName")
//                .build();
//
//        given(userServiceImpl.updateUserProfile(user.getId(), request)).willReturn(user);
//
//        user.setUsername(request.getUsername());
//        user.setEmail(request.getEmail());
//        user.setFirstName(request.getFirstName());
//        user.setLastName(request.getLastName());
//
//        User updatedUser = User.builder()
//                .id(user.getId())
//                .username(request.getUsername())
//                .email(request.getEmail())
//                .firstName(request.getFirstName())
//                .lastName(request.getLastName())
//                .build();
//
//        given(userServiceImpl.updateUserProfile(user.getId(), request)).willReturn(updatedUser);
//
//        ResultActions response = mockMvc.perform(put("/api/users/{id}", user.getId())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request))
//                        .with(SecurityMockMvcRequestPostProcessors.csrf()));
//
//        response.andExpect(status().isOk())
//                .andDo(print())
//                .andExpect(jsonPath("$", is(null)));
////                .andExpect(jsonPath("$.id", is(user.getId())))
////                .andExpect(jsonPath("$.firstName", is(updatedUser.getFirstName())));
//    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldPatchUserProfileSuccessfullyWithGivenId() throws Exception {
        Map<String, Object> object = new HashMap<>();
        object.put("email", "patchEmail@junit.com");

        user.setEmail((String) object.get("email"));
        given(userServiceImpl.patchUserProfile(user.getId(), object)).willReturn(user);

        ResultActions response = mockMvc.perform(patch("/api/users/{id}", user.getId())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(object))
                .with(SecurityMockMvcRequestPostProcessors.csrf()));

        response.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.email", is(object.get("email"))));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldReturnEmptyListOfPostsCreatedByUserWithUserId() throws Exception{

        given(userServiceImpl.getPostsByUserId(user.getId())).willReturn(new ArrayList<Post>());

        ResultActions response = mockMvc.perform(get("/api/users/{id}/posts", user.getId()));

        response.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldReturnListOfPostsCreatedByUserWithUserId() throws Exception{

        List<Post> postsList = new ArrayList<>();
        Post post1 = Post.builder()
                .content("TestPostContentA")
                .title("TestPostTitleA")
                .author(user)
                .status(EPostStatus.PUBLISHED)
                .build();

        Post post2 = Post.builder()
                .content("TestPostContentB")
                .title("TestPostTitleB")
                .author(user)
                .status(EPostStatus.DRAFT)
                .build();

        postsList.add(post1);
        postsList.add(post2);

        user.setPosts(postsList);

        given(userServiceImpl.getPostsByUserId(user.getId())).willReturn(postsList);

        ResultActions response = mockMvc.perform(get("/api/users/{id}/posts", user.getId()));

        response.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(postsList.size())));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldReturnStringWhenDeletingUserProfileWithId() throws Exception{
        given(userServiceImpl.deleteUserAccount(user.getId())).willReturn("Your account has been deleted successfully.");

        ResultActions response = mockMvc.perform(delete("/api/users/{id}", user.getId())
                .with(SecurityMockMvcRequestPostProcessors.csrf()));

        response.andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldReturnErrorMessageWhenDeletingUserThatIsNotAvailable() throws Exception {
        given(userServiceImpl.deleteUserAccount(3L)).willReturn("Not authorized to delete this account!");

        ResultActions response = mockMvc.perform(delete("/api/users/{id}", 3L)
                .with(SecurityMockMvcRequestPostProcessors.csrf()));

        response.andExpect(status().isOk())
                .andDo(print());
    }
}