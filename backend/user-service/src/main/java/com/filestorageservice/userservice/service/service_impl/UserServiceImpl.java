package com.filestorageservice.userservice.service.service_impl;

import com.filestorageservice.userservice.dto.UserDto;
import com.filestorageservice.userservice.exceptions.DuplicateEntryException;
import com.filestorageservice.userservice.exceptions.UserNotFoundException;
import com.filestorageservice.userservice.model.User;
import com.filestorageservice.userservice.payload.ApiResponse;
import com.filestorageservice.userservice.repository.UserRepo;
import com.filestorageservice.userservice.service.service_interface.UserService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private WebClient webClient;

    @Value("${user.default.pic.url}")
    private String defaultPicUrl;

    @Override
    public UserDto createUser(User user) {
        try {
            log.info("user create request : {}", user);
            if (user == null) {
                throw new RuntimeException("user is null");
            }
            if (userRepo.existsByEmail(user.getEmail())) {
                throw new RuntimeException("user already exists");
            }
            if (user.getProfilePicUrl() == null || user.getProfilePicUrl().isEmpty()) {
                user.setProfilePicUrl(defaultPicUrl);
            }
            User createdUser = userRepo.save(user);
            return modelMapper.map(createdUser, UserDto.class);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateEntryException(e.getMessage(), e.getCause());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public UserDto getUserById(String userId) {
        try {
            User user = userRepo.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found with id : " + userId));
            return modelMapper.map(user, UserDto.class);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public UserDto getUserByEmail(String email) {
        log.info("User searched with email : {}", email);

        // Directly throw UserNotFoundException if user is not found
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email : " + email));

        log.info("User found with email : {}", user.getEmail());
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public List<UserDto> getUserByName(String name) {
        try {
            List<User> user = userRepo.findByName(name);
            if (user.isEmpty()) {
                throw new RuntimeException("User not found with name : " + name);
            }
            return user.stream().map((u) -> modelMapper.map(u, UserDto.class)).toList();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public void deleteUserById(String userId) {
        try {
            if (userRepo.existsById(userId)) {
                userRepo.deleteById(userId);
            } else {
                throw new RuntimeException("User not found with id : " + userId);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public void deleteUserByEmail(String email) {
        try {
            if (userRepo.existsByEmail(email)) {
                userRepo.deleteByEmail(email);
            } else {
                throw new RuntimeException("User not found with email : " + email);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public UserDto updateUser(User user) {
        try {
            userRepo.save(user);
            return modelMapper.map(user, UserDto.class);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public UserDto updateUserProfileImage(String userId, MultipartFile profileImage) {
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", profileImage.getResource());
            body.add("bucketName", userId);
            ApiResponse apiResponse = webClient.post()
                    .uri("/upload-file")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(ApiResponse.class)
                    .block();
            assert apiResponse != null;
            String uploadedProfilePicUrl = apiResponse.getData().toString();
            User user = userRepo.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found with id : " + userId));
            user.setProfilePicUrl(uploadedProfilePicUrl);
            userRepo.save(user);
            return modelMapper.map(user, UserDto.class);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
    }
}
