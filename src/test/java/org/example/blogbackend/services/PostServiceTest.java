package org.example.blogbackend.services;

import org.example.blogbackend.models.EPostStatus;
import org.example.blogbackend.models.Post;
import org.example.blogbackend.models.User;
import org.example.blogbackend.repositories.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {

    @Mock
    PostRepository postRepository;

    @InjectMocks
    PostService postService;

    @Test
    public void createNewPostTest() {

        Post post = new Post("Test Post", "Test Content");

        Post expectedPost = new Post(1L, "Test Post", "Test Content", new User(), LocalDateTime.now(), EPostStatus.PUBLISHED, LocalDateTime.now(), false);

        when(postRepository.save(post)).thenReturn(expectedPost);
        Post result = postService.createNewPost(post);
        verify(postRepository).save(post);
        assertEquals(expectedPost, result);
    }

    @Test
    public void getPostById() throws Exception {
        Optional<Post> post = Optional.of(new Post().withId(1L).withTitle("Spring Boot"));

        when(postRepository.findById(1L)).thenReturn(post);

        Post foundPost = postService.findPostById(1L);
        assertThat(foundPost).isEqualTo(post.get());
        verify(postRepository).findById(1L);
    }

    @Test
    public void deletePostById() throws Exception {
        Optional<Post> post = Optional.of(new Post().withId(1L).withTitle("Test Delete"));
        when(postRepository.findById(1L)).thenReturn(post);

        String response = postService.deletePostById(1L);
        assertThat(response).isEqualTo("The post has been deleted successfully.");
    }
}
