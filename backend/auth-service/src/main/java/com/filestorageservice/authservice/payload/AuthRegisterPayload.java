package com.filestorageservice.authservice.payload;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthRegisterPayload {
    private String email;
    private String userId;
    private String password;
    private String firstName;
    private String lastName;
}
