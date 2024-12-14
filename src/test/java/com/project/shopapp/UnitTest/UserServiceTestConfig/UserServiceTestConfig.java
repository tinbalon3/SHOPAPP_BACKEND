package com.project.shopapp.UnitTest.UserServiceTestConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.shopapp.components.JwtTokenUtils;
import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.repositories.RoleRepository;
import com.project.shopapp.repositories.TokenRepository;
import com.project.shopapp.repositories.UserRepository;
import com.project.shopapp.service.ITokenService;
import com.project.shopapp.service.impl.UserServiceImpl;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

@TestConfiguration
public class UserServiceTestConfig {

    @Bean
    public UserServiceImpl userService() {return  Mockito.mock(UserServiceImpl.class);}

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        return Mockito.mock(RedisTemplate.class);
    }

    @Bean
    public ObjectMapper redisObjectMapper() {
        return Mockito.mock(ObjectMapper.class);
    }

    @Bean
    public HashOperations<String, String, Object> hashOperations() {
        return Mockito.mock(HashOperations.class);
    }

    @Bean
    public UserRepository userRepository() {
        return Mockito.mock(UserRepository.class);
    }

    @Bean
    public RoleRepository roleRepository() {
        return Mockito.mock(RoleRepository.class);
    }

    @Bean
    public JwtTokenUtils jwtTokenUtils() {
        return Mockito.mock(JwtTokenUtils.class);
    }

    @Bean
    public TokenRepository tokenRepository() {
        return Mockito.mock(TokenRepository.class);
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return Mockito.mock(AuthenticationManager.class);
    }

    @Bean
    public LocalizationUtils localizationUtils() {
        return Mockito.mock(LocalizationUtils.class);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return Mockito.mock(PasswordEncoder.class);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return Mockito.mock(KafkaTemplate.class);
    }
}
