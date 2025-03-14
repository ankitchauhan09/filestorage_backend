package com.filestorageservice.userservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity(name = "user_list")
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @Column(name = "id", unique = true, nullable = false)
    private String userId;
    @Column(unique = true, name = "email", nullable = false)
    private String email;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "profile_pic_url")
    private String profilePicUrl;
    @Column(name = "last_name")
    private String lastName;
}
