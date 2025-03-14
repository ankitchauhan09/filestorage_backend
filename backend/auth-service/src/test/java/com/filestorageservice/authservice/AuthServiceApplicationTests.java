package com.filestorageservice.authservice;

import com.filestorageservice.authservice.model.User;
import lombok.ToString;
import org.checkerframework.checker.signature.qual.DotSeparatedIdentifiersOrPrimitiveType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AuthServiceApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void createUser( ){
        User user = new User();
        System.out.println(user);
    }

}
