package org.example.emsbackend.repositories;

import org.example.emsbackend.models.Post;
import org.example.emsbackend.models.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends ListCrudRepository<Post, Long> {

    @Query(value = "SELECT * FROM blog.posts WHERE status = 'PUBLISHED';", nativeQuery = true)
    Optional<List<Post>> getPublishedPosts();
}
