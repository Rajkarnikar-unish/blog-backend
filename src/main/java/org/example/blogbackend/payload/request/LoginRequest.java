package org.example.blogbackend.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank
    @NotNull
    private String username;
    @NotBlank
    @NotNull
    private String password;
}
