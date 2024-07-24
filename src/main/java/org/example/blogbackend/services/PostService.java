package org.example.blogbackend.services;

import jakarta.transaction.Transactional;
import org.example.blogbackend.exceptions.PostNotFoundException;
import org.example.blogbackend.models.Post;
import org.example.blogbackend.models.User;
import org.example.blogbackend.repositories.PostRepository;
import org.example.blogbackend.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    private static final Logger logger = LoggerFactory.getLogger(PostService.class);

    @Autowired
    PostRepository postRepository;
    @Autowired
    UserRepository userRepository;

    public List<Post> getPublishedPosts() {
        Optional<List<Post>> opPosts = postRepository.getPublishedPosts();
        if(opPosts.isPresent()) {
            return opPosts.get();
        }
        return new ArrayList<>();
    }

    public List<Post> getPostsByUserId(Long userId) {
        Optional<List<Post>> opPosts = postRepository.getPostsByUserId(userId);
        if(opPosts.isPresent()) {
            return opPosts.get();
        }
        return new ArrayList<>();
    }

    public Post createNewPost(Post post) {
        User currentUser = userRepository
                .findByUsername(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new UsernameNotFoundException("User is not logged in!"));

        if (post.getTitle() != null || post.getContent() != null) {
            post.setAuthor(currentUser);
            currentUser.getPosts().add(post);
            return postRepository.save(post);
        }
        return null;
    }

    public Post publish(Post post, Long draftId) {
        User currentUser = userRepository
                .findByUsername(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new UsernameNotFoundException("User is not logged in!"));
        if (draftId != null) {
            Optional<Post> draft = postRepository.findById(draftId);
            if (draft.isPresent()) {
                Post draftPost = draft.get();
                if (post.getTitle() != null && post.getContent() != null) {
                    draftPost.publish();
                    draftPost.setTitle(post.getTitle());
                    draftPost.setContent(post.getContent());
                    draftPost.setLastUpdated(LocalDateTime.now());
                    return postRepository.save(draftPost);
                }
            }
        }else {
            if (post.getTitle() != null || post.getContent() != null) {
                post.publish();
                post.setAuthor(currentUser);
                currentUser.getPosts().add(post);
                return postRepository.save(post);
            }
        }
        return null;
    }

    public Post findPostById(Long id) throws PostNotFoundException {
        Optional<Post> opPost = postRepository.findById(id);
        if (opPost.isPresent()) {
            return opPost.get();
        }else {
            throw new PostNotFoundException();
        }
    }
    @Transactional
    public String deletePostById(Long id) throws PostNotFoundException {
        Optional<Post> post = postRepository.findById(id);
        System.out.println(post.get());

        if (post.isPresent()) {
            postRepository.delete(post.get());
            return "The post has been deleted successfully.";
        } else {
            throw new PostNotFoundException();
        }
    }
}
