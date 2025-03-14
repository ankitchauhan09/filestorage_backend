package com.filestorageservice.authservice.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor()
@NoArgsConstructor()
public class AuthLoginPayload {
    private String email;
    private String password;
}
