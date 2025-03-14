package com.filestorageservice.userservice.controller;

import com.filestorageservice.userservice.dto.UserDto;
import com.filestorageservice.userservice.exceptions.UserNotFoundException;
import com.filestorageservice.userservice.model.User;
import com.filestorageservice.userservice.payload.ApiResponse;
import com.filestorageservice.userservice.payload.CustomAPIResponse;
import com.filestorageservice.userservice.service.service_interface.UserService;
import com.sun.jdi.request.DuplicateRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(user));
    }

    @GetMapping("")
    public ResponseEntity<?> getUserByEmail(@RequestParam("email") String email) {
        log.info("use get requested with email : {}", email);
        return ResponseEntity.status(HttpStatus.FOUND).body(userService.getUserByEmail(email));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable("userId") String userId) {
        return ResponseEntity.status(HttpStatus.FOUND).body(userService.getUserById(userId));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable("userId") String userId) {
        userService.deleteUserById(userId);
        return ResponseEntity.status(HttpStatus.OK).body(CustomAPIResponse.builder().message("User deleted successfully with id : " + userId).build());
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@RequestBody User user) {
        userService.updateUser(user);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(user, "User updated successfully"));
    }

    @PostMapping(value = "/update/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateUserProfileImage(@RequestParam("userId") String userId,
    @RequestParam("file") MultipartFile profileImage) {
        UserDto user = userService.updateUserProfileImage(userId, profileImage);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(user, "User updated successfully"));
    }

    @DeleteMapping("")
    public ResponseEntity<?> deleteUserByEmail(@RequestParam("email") String email) {
        userService.deleteUserByEmail(email);
        return ResponseEntity.status(HttpStatus.OK).body(CustomAPIResponse.builder().message("User deleted successfully with email : " + email).build());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }

    @ExceptionHandler(DuplicateRequestException.class)
    public ResponseEntity<?> handleDuplicateRequestException(DuplicateRequestException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> handleUserNotFoundException(UserNotFoundException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }


}
