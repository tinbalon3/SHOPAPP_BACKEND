package com.project.shopapp.response.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponse {
    @JsonProperty("message")
    private String message;
    @JsonProperty("token")
    private String token;
    private String tokenType;
    @JsonProperty("refresh_token")
    private String refreshToken;
    @JsonProperty("refresh_token_expired")
    private LocalDateTime refreshTokenExpired;
    @JsonProperty("user_name")
    private String userName;
    private List<String> roles;
    private Long id;
}
