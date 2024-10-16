package com.project.shopapp.service;

import com.project.shopapp.exception.DataNotFoundException;
import com.project.shopapp.models.Token;
import com.project.shopapp.models.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface ITokenService {
    Token addToken(User user, String token, boolean isMobileDevice);
    Token refreshToken(String refreshToken, User user);
    void cleanExpiredTokens();
    LocalDateTime getRefreshExpirationDate(String refreshToken);
    void revokeToken(String token) throws DataNotFoundException;
}
