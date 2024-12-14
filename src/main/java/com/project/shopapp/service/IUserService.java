package com.project.shopapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.shopapp.dto.*;
import com.project.shopapp.exceptions.*;
import com.project.shopapp.models.Role;
import com.project.shopapp.models.User;
import com.project.shopapp.request.UpdatePasswordRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface IUserService {

    User createUser(UserDTO userDTO) throws DataAlreadyExistsException, DataNotFoundException, JsonProcessingException, InvalidDataRegisterException;
    User createAdmin(String adminName, String password, Role role);

    Page<User> getAllUser(String keyword, Pageable pageable);
    User getUserDetails(String token) throws DataNotFoundException;

    String login(UserLoginDTO userLoginDTO) throws EmailNotRegisterException, DataNotFoundException, InvalidPasswordException, UserErrorException;
    void updateEmail(EmailDTO emailDTO) throws Exception;

    void updatePassword(String email, UpdatePasswordRequest updatePasswordRequest) throws Exception;
    User updateUser(Long id, UpdateUserDTO updateUserDTO) throws Exception;

    User getUserDetailsFromRefreshToken(String token) throws Exception;
    User getUserDetailsFromToken(String token) throws Exception;

     void blockOrEnable(Long userId, Boolean active) throws DataNotFoundException;
     Optional<User> getUserById(Long userId) throws DataNotFoundException;
    boolean isAllowed(String userId);

    Optional<User> getUserByEmail(String email) throws DataNotFoundException;


    boolean adminExists();
}
