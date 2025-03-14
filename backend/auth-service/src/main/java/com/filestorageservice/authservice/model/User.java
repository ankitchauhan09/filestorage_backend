package com.filestorageservice.authservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private String profilePicUrl = "";
}
