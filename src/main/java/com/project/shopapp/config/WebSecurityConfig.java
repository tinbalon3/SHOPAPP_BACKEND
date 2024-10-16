package com.project.shopapp.config;

import com.project.shopapp.filter.JwtTokenFilter;
import com.project.shopapp.models.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import java.util.Arrays;
import java.util.List;

import static org.springframework.http.HttpMethod.*;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {


    private final JwtTokenFilter jwtTokenFilter;

    @Value("${api.prefix}")
    private String apiPrefix;

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
                                        String.format("%s/users/login", apiPrefix)
                                ).permitAll()
                                .requestMatchers(POST,
                                        String.format("%s/users/refreshToken", apiPrefix),
                                        String.format("%s/users/revoke-token", apiPrefix),
                                        String.format("%s/vnpay/submitOrder", apiPrefix)
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
                                        String.format("%s/users/verify", apiPrefix),
                                        String.format("%s/rating", apiPrefix),
                                        String.format("%s/coupons/calculate", apiPrefix),
                                        String.format("%s/vnpay/getPaymentInfo", apiPrefix)
                                ).permitAll()
                                .requestMatchers(POST,
                                        String.format("%s/orders", apiPrefix),
                                        String.format("%s/cart/add/*", apiPrefix)

                                ).hasRole("USER")

                                .requestMatchers(PUT,
                                        String.format("%s/users/update_password/*", apiPrefix)
                                ).hasRole("USER")
                                .requestMatchers(PUT,
                                        String.format("%s/orders/**", apiPrefix),
                                        String.format("%s/cart/update/*", apiPrefix),
                                        String.format("%s/users/reset-password/*", apiPrefix),
                                        String.format("%s/users/blockOrEnable/**", apiPrefix)
                                ).hasRole("ADMIN")

                                .requestMatchers(GET,
                                        String.format("%s/cart/get/*", apiPrefix)

                                ).hasRole("USER")

                                .requestMatchers(GET,
                                        String.format("%s/orders/get-orders", apiPrefix),
                                        String.format("%s/users", apiPrefix)
                                ).hasRole("ADMIN")

                                .requestMatchers(DELETE,
                                        String.format("%s/cart/remove/*", apiPrefix)
                                ).hasRole("USER")

                                .anyRequest().authenticated()
                );

        return http.build();
    }


}
