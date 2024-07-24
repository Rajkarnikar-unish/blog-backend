package org.example.blogbackend.repositories;

import org.example.blogbackend.models.Post;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends ListCrudRepository<Post, Long> {

    @Query(value = "SELECT * FROM blog.posts WHERE status = 'PUBLISHED';", nativeQuery = true)
    Optional<List<Post>> getPublishedPosts();

    @Query(value = "SELECT * FROM blog.posts WHERE user_id = :id", nativeQuery = true)
    Optional<List<Post>> getPostsByUserId(Long id);
}
