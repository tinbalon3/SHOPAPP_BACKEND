package com.project.shopapp.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.shopapp.exceptions.OTPExpiredException;
import com.project.shopapp.models.User;
import com.project.shopapp.repositories.UserRepository;
import com.project.shopapp.service.IVerifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class VerifyServiceImpl extends BaseRedisServiceImpl implements IVerifyService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean verifyEmailCodeToDo(String verificationCode, String email) throws JsonProcessingException, OTPExpiredException {
        String user_OTP_Key = "User_OTP_reset_password:" + email;
        Map<String, String> user_OTP = (Map<String, String>) getMap(user_OTP_Key, String.class, String.class);
        if(!user_OTP.isEmpty()){
            String code = user_OTP.get(user_OTP_Key);
            if(code.equals(verificationCode)) {
                return true;
            }
            return false;
        }
        else
            throw new OTPExpiredException("Mã OTP đã hết hạn. Vui lòng yêu cầu lại mã OTP.");


    }
    @Override
    public boolean verifyRegisterCode(String verificationCode,String email) throws JsonProcessingException {
        String user_OTP_Key = "User_OTP_register:" + email;
        String user_INFO_Key = "User_INFO_register:" + email;

        Map<String, String> user_OTP = (Map<String, String>) getMap(user_OTP_Key, String.class, String.class);
        Map<String, User> user_INFO = (Map<String, User>) getMap(user_INFO_Key, String.class, User.class);

        if (user_OTP.isEmpty() || user_INFO.get(user_INFO_Key) == null ) {
            return false;
        }
        else {
            String code = user_OTP.get(user_OTP_Key);
            if(code.equals(verificationCode)) {
                User user = user_INFO.get(user_INFO_Key);
                user.setEnabled(true);
                userRepository.save(user);
                return true;
            }
            return false;

        }
    }


}
