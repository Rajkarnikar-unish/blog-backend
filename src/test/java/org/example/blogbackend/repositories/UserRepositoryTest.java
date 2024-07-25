package org.example.blogbackend.repositories;

import org.example.blogbackend.models.Post;
import org.example.blogbackend.models.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveUserToDB() {
        User user = new User("TestUserA", "email@junit.com", "FirstName", "LastName", "PasswordA123","imageUrl");
        userRepository.save(user);

        assertThat(user.getId()).isGreaterThan(0L);
    }
}
