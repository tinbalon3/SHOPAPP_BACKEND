package com.project.shopapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.shopapp.dto.*;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.models.CustomOAuth2User;
import com.project.shopapp.models.Order;
import com.project.shopapp.models.Role;
import com.project.shopapp.models.User;
import jakarta.mail.MessagingException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

public interface IUserService {

    User createUser(UserDTO userDTO) throws Exception;
    User createAdmin(String adminName, String password, Role role);
    boolean adminExists();
    Page<User> getAllUser(String keyword, Pageable pageable);
    User getUserDetails(String token) throws DataNotFoundException;

    String login(UserLoginDTO userLoginDTO) throws Exception;
    void updateEmail(Long id, EmailDTO emailDTO) throws Exception;

    void updatePassword(Long id, PasswordDTO passwordDTO) throws Exception;
    User updateUser(Long id, UpdateUserDTO updateUserDTO) throws Exception;

    User getUserDetailsFromRefreshToken(String token) throws Exception;
    User getUserDetailsFromToken(String token) throws Exception;
     void resetPassword(Long userId) throws DataNotFoundException, MessagingException, UnsupportedEncodingException;
     void blockOrEnable(Long userId, Boolean active) throws DataNotFoundException;
     Optional<User> getUserById(Long userId) throws DataNotFoundException;

    void sendPasswordResetCodeEmail(User user) throws MessagingException, UnsupportedEncodingException;

    void sendVerificationEmail(String email,String VerificationCode) throws MessagingException, UnsupportedEncodingException;

    void sendChangeEmailCode(EmailDTO emailDTO,User user) throws MessagingException, UnsupportedEncodingException, JsonProcessingException;
    boolean isAllowed(String userId);

    boolean verify(String code,String email) throws JsonProcessingException, DataNotFoundException;

    void sendMailOrderSuccessfully(OrderDTO order) throws DataNotFoundException;

    void sendErrorMailOnInvalidEmail(OrderDTO order) throws DataNotFoundException;

}
