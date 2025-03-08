package org.thoughtlabs.blogbackend.payload.request;

import jakarta.validation.constraints.*;

public class PasswordResetBody {

    @NotBlank
    @NotNull
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=])(?=\\S+$).{6,32}$")
    private String password;

    @NotBlank
    @NotNull
    private String token;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
