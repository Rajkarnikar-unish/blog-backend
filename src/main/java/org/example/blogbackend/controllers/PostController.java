package org.example.blogbackend.controllers;

import org.example.blogbackend.exceptions.PostNotFoundException;
import org.example.blogbackend.models.Post;
import org.example.blogbackend.services.PostService;
import org.example.blogbackend.services.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    PostService postService;

    @Autowired
    UserServiceImpl userService;

    @GetMapping
    public ResponseEntity<?> getAllPosts() {
        List<Post> posts = postService.getPublishedPosts();
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable Long id) throws Exception {
        Post post = postService.findPostById(id);
        return ResponseEntity.ok(post);
    }

//    @PostMapping("/new-post")
//    public ResponseEntity<Post> createNewPost(@RequestBody Post post) throws PostNotFoundException {
//        if (post.getId() == null || post.getId() == 0) {
//            postService.createNewPost(post);
//        } else {
//            Post existingPost = postService.findPostById(post.getId());
//            if (existingPost != null) {
//                if (existingPost.getStatus() == )
//            }
//        }
//        Post result = postService.createNewPost(post);
//        return ResponseEntity.ok(result);
//    }

    @PostMapping("/new-post")
    public ResponseEntity<Post> createNewPost(@RequestBody Post post) {
        return ResponseEntity.ok(postService.createNewPost(post));
    }

    @PutMapping("/publish")
    public ResponseEntity<Post> publishPost(@RequestBody Post post, @RequestParam(required = false) Long draftId) {
        return ResponseEntity.ok(postService.publish(post, draftId));
    }

//    @PostMapping("/save-draft")
//    public ResponseEntity<Post> saveForLater(@RequestBody Post post, @RequestParam(required = false) Long draftId) {
//        Post newPost = postService.createNewPost(post, draftId);
//        return ResponseEntity.ok(newPost);
//    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePostById(@PathVariable Long id) throws PostNotFoundException {
        return ResponseEntity.ok(postService.deletePostById(id));
    }
}
