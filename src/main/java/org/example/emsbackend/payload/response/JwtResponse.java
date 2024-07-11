package org.example.emsbackend.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class JwtResponse {
    private String token;
    private String type="Bearer";
    private String refreshToken;
    @Getter
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private List<String> roles;
    private String profileImageUrl;

    public JwtResponse(String accessToken, String refreshToken, Long id, String username, String firstName, String lastName, String email, List<String> roles, String profileImageUrl) {
        this.token = accessToken;
        this.refreshToken = refreshToken;
        this.id = id;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.roles = roles;
        this.profileImageUrl = profileImageUrl;
    }

}
