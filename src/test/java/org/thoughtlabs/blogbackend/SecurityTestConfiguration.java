package org.thoughtlabs.blogbackend;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@TestConfiguration
public class SecurityTestConfiguration {

    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        UserDetails userDetails = User.withUsername("user")
                .password("PasswordA123")
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(userDetails);
    }
}
