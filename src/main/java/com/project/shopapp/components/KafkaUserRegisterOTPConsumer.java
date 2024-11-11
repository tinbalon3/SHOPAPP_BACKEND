package com.project.shopapp.components;

import com.project.shopapp.dto.OrderDTO;
import com.project.shopapp.dto.RegisterObject;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.models.User;
import com.project.shopapp.service.IUserService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Optional;

@Component
public class KafkaUserRegisterOTPConsumer {
    @Autowired
    private IUserService iUserService;

    @KafkaListener(id = "sendOtpToEmail",  // Đặt id cho listener
            topics = "user_register_otp_email_topic",  // Chỉ định topic
            groupId = "email-consumer-group",  // Chỉ định groupId
            concurrency = "3"  // Chạy 3 consumer instances song song)
    )
    public void handleOrder(RegisterObject registerObject) throws MessagingException, UnsupportedEncodingException {
            // Thử xử lý và gửi email
            iUserService.sendVerificationEmail(registerObject.getEmail(), registerObject.getVerificationCode());

    }

}
