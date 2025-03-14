package com.filestorageservice.userservice;

import com.filestorageservice.userservice.model.User;
import com.filestorageservice.userservice.repository.UserRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UserServiceApplicationTests {

    @Test
    void contextLoads() {
    }


    @Autowired
    private UserRepo userRepo;

    @Test
    void TestRepo() {
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@doe.com");
        user.setUserId("1234");
        userRepo.save(user);
    }

}
