package org.thoughtlabs.blogbackend.payload.request;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequest {

    private String username;

    private String firstName;

    private String lastName;

    private String email;
}
