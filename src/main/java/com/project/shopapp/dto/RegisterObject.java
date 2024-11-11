package com.project.shopapp.dto;

import com.project.shopapp.models.User;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterObject {
    private String email;
    private String verificationCode;
}
