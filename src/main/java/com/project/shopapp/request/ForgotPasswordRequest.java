package com.project.shopapp.request;

import lombok.*;

@AllArgsConstructor
@Data
@Getter
@Setter
@NoArgsConstructor
public class ForgotPasswordRequest {
    private String email;
}
