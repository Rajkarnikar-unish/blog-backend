package org.thoughtlabs.blogbackend.repositories;

import org.thoughtlabs.blogbackend.models.User;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @Order(1)
    @Rollback(false)
    void shouldSaveUserToDB() {
        User user = new User("TestUserA", "email@junit.com", "FirstName", "LastName", "PasswordA123","imageUrl");
        userRepository.save(user);

        assertThat(user.getId()).isGreaterThan(0L);
    }

    @Test
    @Order(2)
    void shouldGetUserById() {
        User user = userRepository.findById(1L).get();
        System.out.println(user);
        assertThat(user.getId()).isEqualTo(1L);
    }

    @Test
    @Order(3)
    void shouldReturnTrueForExistsByUsername() {
        Boolean userExists = userRepository.existsByUsername("TestUserA");
        assertThat(userExists).isTrue();
    }

    @Test
    @Order(4)
    void shouldReturnTrueForExistsByEmail() {
        Boolean emailExists = userRepository.existsByEmail("email@junit.com");
        assertThat(emailExists).isTrue();
    }

    @Test
    @Order(5)
    void shouldReturnUpdatedUserWithUserUpdateRequest() {
        User user = userRepository.findById(1L).get();
        user.setEmail("updateEmail@junit.com");
        user.setUsername("TestUpdatedA");
        User updatedUser = userRepository.save(user);

        assertThat(updatedUser.getEmail()).isEqualTo("updateEmail@junit.com");
        assertThat(updatedUser.getUsername()).isEqualTo("TestUpdatedA");
    }

    @Test
    @Order(6)
    void shouldDeleteUserById() {
        userRepository.deleteById(1L);
        Optional<User> optionalUser = userRepository.findById(1L);

        assertThat(optionalUser).isEmpty();
    }

}
