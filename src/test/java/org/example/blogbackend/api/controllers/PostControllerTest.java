package org.example.blogbackend.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.example.blogbackend.controllers.PostController;
import org.example.blogbackend.exceptions.PostNotFoundException;
import org.example.blogbackend.models.Post;
import org.example.blogbackend.services.PostService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(PostController.class)
@WithMockUser
public class PostControllerTest {

    @MockBean
    PostService postService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    public void testCreateNewPost() throws Exception {
        Post post = new Post().withId(1L).withTitle("Test Post");
        when(postService.createNewPost(post)).thenReturn(post);

        ResultActions result = mockMvc.perform(post("/api/posts/new-post")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(post)))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetById() throws Exception {
        Post post = new Post().withId(1L).withTitle("Spring Boot");

        when(postService.findPostById(1L)).thenReturn(post);

        ResultActions resultActions = mockMvc.perform(get("/api/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Spring Boot"));

        verify(postService).findPostById(1L);
    }

    @Test
    public void testGetByIdNotFound() throws Exception {

        when(postService.findPostById(1L)).thenThrow(new PostNotFoundException());

        ResultActions result = mockMvc.perform(get("/api/posts/1"))
                .andExpect(status().isNotFound());

        verify(postService).findPostById(1L);
    }

    // @Test
    // public void testDeletePostById() throws Exception {
    // when(postService.deletePostById(1L)).thenThrow();
    //
    // ResultActions result= mockMvc.perform(delete("/api/posts/1"))
    // .andExpect(status().isOk());
    //
    //// verify(postService).deletePostById(1L);
    // }
}
