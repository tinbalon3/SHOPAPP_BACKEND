package com.project.shopapp.service.impl;

import com.amazonaws.services.kms.model.NotFoundException;
import com.project.shopapp.components.JwtTokenUtils;
import com.project.shopapp.exception.DataNotFoundException;
import com.project.shopapp.models.Token;
import com.project.shopapp.models.User;
import com.project.shopapp.repositories.TokenRepository;
import com.project.shopapp.service.ITokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements ITokenService {
    private static final int MAX_TOKENS = 3;
    @Value("${jwt.longExpiration}")
    private int expiration;

    @Value("${jwt.expiration-refresh-token}")
    private int expirationRefreshToken;

    private final TokenRepository tokenRepository;

    private final JwtTokenUtils jwtTokenUtils;
    @Override
    @Transactional
    public Token addToken(User user, String token, boolean isMobileDevice) {
        List<Token> userTokens = tokenRepository.findByUser(user);
        int tokenCount = userTokens.size();
        //Số lượng token vượt quá giới hạn, xóa một token cũ
        if(tokenCount >= MAX_TOKENS){
            //kiểm tra xem trong danh sách userTokens có tồn tại ít nhất
            //một token không phải là thiết bị di động (non-mobile)
            boolean hasNonMobileToken = !userTokens.stream().allMatch(Token::isMobile);
            Token tokenToDelete;
            if(hasNonMobileToken){
                //nếu tất cả đều là mobile thì xóa thằng đầu tiên
                tokenToDelete = userTokens.stream().filter(userToken -> !userToken.isMobile()).findFirst().orElse(userTokens.get(0));
            } else {
                //tất cả các token đều là thiết bị di động,
                //chúng ta sẽ xóa token đầu tiên trong danh sách
                tokenToDelete = userTokens.get(0);
            }
           tokenRepository.delete(tokenToDelete);
        }
        long expirationInSeconds = expiration;
        LocalDateTime expirationDateTime = LocalDateTime.now().plusSeconds(expirationInSeconds);
        //Tạo mới một token cho người dùng
        Token newToken = Token.builder()
                .user(user)
                .token(token)
                .revoked(false)
                .expired(false)
                .tokenType("Bearer")
                .expirationDate(expirationDateTime)
                .isMobile(isMobileDevice)
                .build();
        newToken.setRefreshToken(UUID.randomUUID().toString());
        newToken.setRefreshExpirationDate(LocalDateTime.now().plusSeconds(expirationRefreshToken));
        tokenRepository.save(newToken);
        return newToken;
    }

    @Override
    @Transactional
    public Token refreshToken(String refreshToken, User userDetail) {
        // Tìm refresh token trong cơ sở dữ liệu
        Token storedToken = tokenRepository.findByRefreshToken(refreshToken).orElseThrow(
                () -> new RuntimeException("Invalid refresh token"));;
        // Kiểm tra token có bị thu hồi (revoked) hoặc hết hạn không
        if (storedToken.isRevoked() || storedToken.isExpired() ||
                storedToken.getRefreshExpirationDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token is invalid or expired");
        }

        // Tạo JWT token mới
        String newToken = jwtTokenUtils.generateToken(userDetail);

        // Tạo refreshToken mới
        String newRefreshToken = UUID.randomUUID().toString();

        // Cập nhật thông tin token và refreshToken cũ
        storedToken.setToken(newToken); // Cập nhật JWT token mới
        storedToken.setRefreshToken(newRefreshToken); // Cập nhật refresh token mới
        storedToken.setExpirationDate(LocalDateTime.now().plusSeconds(expiration)); // Cập nhật thời gian hết hạn token
        storedToken.setRefreshExpirationDate(LocalDateTime.now().plusSeconds(expirationRefreshToken)); // Cập nhật thời gian hết hạn refresh token
        storedToken.setRevoked(false); // Đảm bảo token không bị thu hồi
        storedToken.setExpired(false); // Đảm bảo token chưa hết hạn

        // Lưu thay đổi vào cơ sở dữ liệu
        tokenRepository.save(storedToken);

        return storedToken;
    }

    @Scheduled(cron = "0 0 * * * ?")  // Chạy mỗi giờ
    @Override
    public void cleanExpiredTokens() {
        tokenRepository.deleteByExpirationDateBefore(new Date());
    }

    @Override
    public LocalDateTime getRefreshExpirationDate(String refreshToken) {
        return tokenRepository.findRefreshExpirationDateByToken(refreshToken).orElseThrow(
                () -> new NotFoundException("Không tìm thấy ExpirationDateByToken với refreshToken:" + refreshToken)
        );
    }

    @Override
    public void revokeToken(String refreshToken) throws DataNotFoundException {
        Token existingToken = tokenRepository.findByRefreshToken(refreshToken).orElseThrow(
                () -> new DataNotFoundException("Not found refresh token")
        );
        existingToken.setRevoked(true);
        tokenRepository.save(existingToken);

    }
}



