package com.project.shopapp.components;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.shopapp.dto.RegisterObject;


import com.project.shopapp.service.ISendEmailService;

import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;

import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;


@Component
public class KafkaUserRegisterOTPConsumer {
    @Autowired
    private ISendEmailService emailService;

    @KafkaListener(id = "sendOtpToEmail",  // Đặt id cho listener
            topics = "user_register_otp_email_topic",  // Chỉ định topic
            groupId = "email-consumer-group",  // Chỉ định groupId
            concurrency = "3"  // Chạy 3 consumer instances song song)
    )
    public void handleRegisterOTPCode(RegisterObject registerObject) throws MessagingException, UnsupportedEncodingException, JsonProcessingException {
            // Thử xử lý và gửi email
            emailService.sendVerificationEmailCode(registerObject.getEmail(), registerObject.getVerificationCode());

    }

}
