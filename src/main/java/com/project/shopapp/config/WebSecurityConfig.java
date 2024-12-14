package com.project.shopapp.config;

import com.project.shopapp.filter.CustomOAuth2UserService;
import com.project.shopapp.filter.JwtTokenFilter;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;


import static org.springframework.http.HttpMethod.*;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {


    private final JwtTokenFilter jwtTokenFilter;

    @Value("${api.prefix}")
    private String apiPrefix;

    private final CustomOAuth2UserService oauthUserService;



    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http

                .csrf(csrf -> csrf
                        .disable() // Disable CSRF for simplicity, enable if needed
                )
                .sessionManagement(sessionManagement ->
                        sessionManagement
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Example configuration
                )
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers(
                                        String.format("%s/users/register", apiPrefix),
                                        String.format("%s/users/auth/login", apiPrefix)

                                ).permitAll()
                                .requestMatchers(POST,
                                        String.format("%s/token/refreshToken", apiPrefix),

                                        String.format("%s/users/reset-password/send-verification-code", apiPrefix)
                                ).permitAll()
                                .requestMatchers(GET,
                                        String.format("%s/actuator/health", apiPrefix),
                                        "/api-docs",
                                        "/api-docs/**",
                                        "/swagger-resources",
                                        "/swagger-resources/**",
                                        "/configuration/ui",
                                        "/configuration/security",
                                        "/swagger-ui/**",
                                        "/swagger-ui.html",
                                        "/swagger-ui/index.html"
                                ).permitAll()
                                .requestMatchers(GET,
                                        String.format("%s/roles**", apiPrefix),
                                        String.format("%s/categories**", apiPrefix),
                                        String.format("%s/products/**", apiPrefix),
                                        String.format("%s/products/images/*", apiPrefix),
                                        String.format("%s/orders/**", apiPrefix),
                                        String.format("%s/order_details/**", apiPrefix),
                                        String.format("%s/refreshToken/**", apiPrefix),
                                        String.format("%s/users/reset-password/check-email-exist", apiPrefix),
                                        String.format("%s/rating", apiPrefix),
                                        String.format("%s/coupons/calculate", apiPrefix),
                                        String.format("%s/vnpay/getPaymentInfo", apiPrefix),
                                        String.format("%s/rating/stats/*", apiPrefix),
                                        String.format("%s/users/auth/callback", apiPrefix),
                                        String.format("%s/users/auth/googleLogin", apiPrefix),
                                        String.format("%s/token/refreshExpirationDate/*", apiPrefix),
                                        String.format("%s/products/getPrice", apiPrefix)


                                ).permitAll()
                                .requestMatchers(PUT,
                                        String.format("%s/users/logout", apiPrefix),
                                        String.format("%s/users/reset-password/send-verification-code", apiPrefix),
                                        String.format("%s/users/reset-password/change-pass", apiPrefix),
                                        String.format("%s/users/register/verify", apiPrefix),
                                        String.format("%s/users/email/verify", apiPrefix)
                                )
                                .permitAll()
                                .requestMatchers(POST,
                                        String.format("%s/orders", apiPrefix),
                                        String.format("%s/cart/add/*", apiPrefix),
                                        String.format("%s/vnpay/submitOrder", apiPrefix),
                                        String.format("%s/rating", apiPrefix)
                                ).hasRole("USER")

                                .requestMatchers(PUT,
                                        String.format("%s/users/update-password/*", apiPrefix),
//                                        String.format("%s/users/reset-password/send-verification-code", apiPrefix),
                                        String.format("%s/users/update-email", apiPrefix)

                                ).hasRole("USER")
                                .requestMatchers(PUT,
                                        String.format("%s/orders/**", apiPrefix),
                                        String.format("%s/cart/update/*", apiPrefix),
                                        String.format("%s/users/blockOrEnable/**", apiPrefix),
                                        String.format("%s/coupons/active/**", apiPrefix)
                                ).hasRole("ADMIN")

                                .requestMatchers(GET,
                                        String.format("%s/cart/get/*", apiPrefix),

                                        String.format("%s/orders/order-history/*", apiPrefix),
                                        String.format("%s/orders/*", apiPrefix)
                                ).hasRole("USER")

                                .requestMatchers(GET,
                                        String.format("%s/orders/get-orders", apiPrefix),
                                        String.format("%s/users", apiPrefix)
                                ).hasRole("ADMIN")

                                .requestMatchers(DELETE,
                                        String.format("%s/cart/remove/**", apiPrefix)
                                ).hasRole("USER")

                                .anyRequest().authenticated()
                )
                .oauth2Login(oauth2Login ->
                        oauth2Login
                                .userInfoEndpoint(userInfo ->
                                        userInfo.userService(oauthUserService))


                );

        return http.build();
    }


}
