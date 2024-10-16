package com.project.shopapp.components;

import com.project.shopapp.models.Token;
import com.project.shopapp.models.User;
import com.project.shopapp.repositories.TokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class JwtTokenUtils {



    @Value("${jwt.shortExpiration}")
    private int expiration; //

    @Value("${jwt.secretKey}")
    private String secretKey;

    private final TokenRepository tokenRepository;
    public String generateToken(User user){
        // Properties => Claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("phoneNumber", user.getPhoneNumber());
        claims.put("userId", user.getId());

        try {
            Date expirationDate = new Date(System.currentTimeMillis() + expiration * 1000L);

            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(user.getPhoneNumber())
                    .setExpiration(expirationDate) // Thay đổi thời gian hết hạn
                    .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                    .compact();
        } catch (Exception e) {
            // Xử lý lỗi
            e.printStackTrace();
            return null;
        }
    }

    private Key getSignInKey(){
        byte[] bytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(bytes);
    }
    private String generateSecretKey(){
        SecureRandom secureRandom = new SecureRandom();
        byte[] keyBytes = new byte[32];//256-bit key
        secureRandom.nextBytes(keyBytes);
        String secretKey = Encoders.BASE64.encode(keyBytes);
        return secretKey;
    }
    private Claims extractAllClaims(String token){
        return Jwts.parser()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    public String extractPhoneNumber(String token){
        return extractClaims(token,Claims::getSubject);
    }
    public Long extractUserId(String token){
        return extractClaims(token, claims -> claims.get("userId", Long.class));
    }
    public <T> T extractClaims(String token, Function<Claims,T> claimsResolver){
        final Claims claims = this.extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    //check expiration
    public boolean isTokenExpired(String token){
        Date expirationDate = this.extractClaims(token,Claims::getExpiration);
        return expirationDate.before(new Date());
    }

    public boolean validateToken(String token, User userDetails){
        String phoneNumber = extractPhoneNumber(token);
        Token existingToken = tokenRepository.findByToken(token);
        if(existingToken == null || existingToken.isRevoked() == true || !userDetails.isActive()){
            return false;
        }
        return (phoneNumber.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
