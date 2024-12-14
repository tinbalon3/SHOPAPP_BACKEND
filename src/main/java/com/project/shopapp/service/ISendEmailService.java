package com.project.shopapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.shopapp.dto.EmailDTO;
import com.project.shopapp.dto.OrderDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.exceptions.EmailNotRegisterException;
import com.project.shopapp.models.User;
import jakarta.mail.MessagingException;

import java.io.UnsupportedEncodingException;

public interface ISendEmailService {
    void sendPasswordResetEmailCode(User user) throws MessagingException, UnsupportedEncodingException, JsonProcessingException;
    void sendVerificationEmailCode(String email,String VerificationCode) throws MessagingException, UnsupportedEncodingException, JsonProcessingException;
    void sendChangeEmailCode(EmailDTO emailDTO, User user) throws MessagingException, UnsupportedEncodingException, JsonProcessingException;
    void sendMailOrderSuccessfully(OrderDTO order) throws DataNotFoundException;
    void sendErrorMailOnInvalidEmail(OrderDTO order) throws DataNotFoundException;
    void resetPassword(String email) throws DataNotFoundException, MessagingException, UnsupportedEncodingException, EmailNotRegisterException;
}
