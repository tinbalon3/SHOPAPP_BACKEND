package com.project.shopapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.exceptions.OTPExpiredException;

public interface IVerifyService {
    boolean verifyEmailCodeToDo(String verificationCode, String email) throws JsonProcessingException, OTPExpiredException;

    boolean verifyRegisterCode(String code, String email) throws JsonProcessingException, DataNotFoundException;

}
