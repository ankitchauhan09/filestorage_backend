package com.filestorageservice.userservice.service.service_interface;

import com.filestorageservice.userservice.dto.UserDto;
import com.filestorageservice.userservice.model.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {
    public UserDto createUser(User user);
    public UserDto getUserById(String userId);
    public UserDto getUserByEmail(String email);
    public List<UserDto> getUserByName(String name);
    public void deleteUserById(String userId);
    public void deleteUserByEmail(String email);
    public UserDto updateUser(User user);

    UserDto updateUserProfileImage(String userId, MultipartFile profileImage);
}
