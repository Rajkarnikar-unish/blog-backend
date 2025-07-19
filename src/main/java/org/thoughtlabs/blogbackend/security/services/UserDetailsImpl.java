package org.thoughtlabs.blogbackend.security.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.thoughtlabs.blogbackend.models.AuthProvider;
import org.thoughtlabs.blogbackend.models.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class UserDetailsImpl extends User implements OAuth2User, UserDetails {

    private static final long serialVersionUID  = 1L;

    @Getter
    private Long id;
    private String username;
    @Getter
    @Setter
    private String email;
    @Getter
    @Setter
    private String firstName;
    @Getter
    @Setter
    private String lastName;
    @JsonIgnore
    private String password;
    @Getter
    @Setter
    private String profileImageUrl;
    @Getter
    @Setter
    private Boolean isEmailVerified;
    @Getter
    @Setter
    private AuthProvider provider;

    private Collection<? extends GrantedAuthority> authorities;

    private Map<String, Object> attributes;

    //Constructor
    public UserDetailsImpl(Long id, String username, String firstName, String lastName, String email, String password, Boolean isEmailVerified, String profileImageUrl, AuthProvider provider, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.isEmailVerified = isEmailVerified;
        this.profileImageUrl = profileImageUrl;
        this.provider = provider;
        this.authorities = authorities;
    }

    public static UserDetailsImpl build(User user) {
        /*
         * Converting Set<Role> into List of GrantedAuthority
         * */
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList());

        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPassword(),
                user.isEmailVerified(),
                user.getProfileImageUrl(),
                user.getProvider(),
                authorities);
    }

    public static UserDetailsImpl build(User user, Map<String, Object> attributes) {
        UserDetailsImpl userDetailsImpl = UserDetailsImpl.build(user);
        userDetailsImpl.setAttributes(attributes);
        return userDetailsImpl;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (this==obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        UserDetailsImpl user = (UserDetailsImpl) obj;
        return Objects.equals(id, user.id);
    }

    @Override
    public String getName() {
        return String.valueOf(id);
    }

    @Override
    public String toString() {
        return "UserDetailsImpl{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", password='" + password + '\'' +
                ", profileImageUrl='" + profileImageUrl + '\'' +
                ", provider='" + provider + '\'' +
                ", authorities=" + authorities +
                ", attributes=" + attributes +
                '}';
    }
}
