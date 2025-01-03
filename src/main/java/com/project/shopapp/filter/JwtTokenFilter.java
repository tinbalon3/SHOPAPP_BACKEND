package com.project.shopapp.filter;

import com.project.shopapp.components.JwtTokenUtils;

import com.project.shopapp.models.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    @Value("${api.prefix}")
    private String apiPrefix;

    private final CustomerUserDetailsService customerUserDetailsService;

    private final JwtTokenUtils jwtTokenUtils;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

            if (isBypassToken(request)) {
                filterChain.doFilter(request, response);
                return;
            }

            final String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer")) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                return;
            }

            final String token = authHeader.substring(7);
            try{
                String email = jwtTokenUtils.validateToken(token);
                    if (email != null && SecurityContextHolder.getContext() != null) {
                        UserDetails userDetails = customerUserDetailsService.loadUserByUsername(email);
                            if (jwtTokenUtils.validateTokenWithUser(email, (User) userDetails)) {
                                UsernamePasswordAuthenticationToken authenticationToken =
                                        new UsernamePasswordAuthenticationToken(
                                                userDetails,
                                                null,
                                                userDetails.getAuthorities()
                                        );
                                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                                request.getSession(true)
                                        .setAttribute(SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
                            }
                    }

            } catch ( Exception e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // Đặt trạng thái 401
                response.setContentType("application/json"); // Loại nội dung là JSON
                response.setCharacterEncoding("UTF-8"); // Đặt mã hóa UTF-8

                // Ghi thông báo lỗi từ exception vào response
                String errorMessage = e.getMessage() != null ? e.getMessage() : "Có lỗi trong quá trình xác thực token";
                response.getWriter().write(String.format("{\"status\":401, \"message\":\"%s\"}", errorMessage));
                return;
            }



        filterChain.doFilter(request, response);

    }

    private boolean isBypassToken(@NonNull HttpServletRequest request){
        final List<Pair<String,String>> bypassTokens = Arrays.asList(
                Pair.of(String.format("%s/users/email/verify",apiPrefix),"PUT"),
                Pair.of(String.format("%s/users/reset-password/change-pass",apiPrefix),"PUT"),
                Pair.of(String.format("%s/users/reset-password/check-email-exist",apiPrefix),"GET"),
                Pair.of(String.format("%s/users/reset-password/send-verification-code",apiPrefix),"POST"),
                Pair.of(String.format("%s/products/getPrice",apiPrefix),"GET"),
                Pair.of(String.format("%s/token/refreshExpirationDate/*",apiPrefix),"GET"),
                Pair.of(String.format("%s/users/auth/googleLogin",apiPrefix),"GET"),
                Pair.of(String.format("%s/users/auth/callback",apiPrefix),"GET"),
                //vnpay

                Pair.of(String.format("%s/vnpay/getPaymentInfo",apiPrefix),"GET"),
                //coupon
                Pair.of(String.format("%s/coupons/calculate",apiPrefix),"GET"),
                //healthcheck
                Pair.of(String.format("%s/actuator/health",apiPrefix),"GET"),
                //api normal
                Pair.of(String.format("%s/rating", apiPrefix),"GET"),
                Pair.of(String.format("%s/roles",apiPrefix),"GET"),
                Pair.of(String.format("%s/products",apiPrefix),"GET"),
                Pair.of(String.format("%s/products/images/*",apiPrefix),"GET"),
                Pair.of(String.format("%s/categories",apiPrefix),"GET"),
                Pair.of(String.format("%s/order_details/**",apiPrefix),"GET"),
                Pair.of(String.format("%s/users/register",apiPrefix),"POST"),
                Pair.of(String.format("%s/users/auth/login",apiPrefix),"POST"),

                Pair.of(String.format("%s/token/refreshToken",apiPrefix),"POST"),
                Pair.of(String.format("%s/users/logout",apiPrefix),"PUT"),
                Pair.of(String.format("%s/refreshToken/**",apiPrefix),"GET"),
                Pair.of(String.format("%s/users/register/verify", apiPrefix),"PUT"),
                Pair.of(String.format("%s/users/saveUser", apiPrefix),"POST"),
                //api swagger
                Pair.of("/api-docs","GET"),
                Pair.of("/api-docs/**","GET"),
                Pair.of("/swagger-resources","GET"),
                Pair.of("/swagger-resources/**","GET"),
                Pair.of("/configuration/ui","GET"),
                Pair.of("/configuration/security","GET"),
                Pair.of("/swagger-ui/**","GET"),
                Pair.of("/swagger-ui.html", "GET"),
                Pair.of("/swagger-ui/index.html", "GET"),
                Pair.of("/swagger-ui/swagger-ui-bundle.js", "GET"),
                Pair.of("/swagger-ui/swagger-ui-standalone-preset.js", "GET"),
                Pair.of("/swagger-ui/swagger-initializer.js", "GET"),
                Pair.of("/swagger-ui/favicon-32x32.png", "GET"),
                Pair.of("/swagger-ui/swagger-ui.css", "GET"),
                Pair.of("/api-docs/swagger-config", "GET")

        );
        String requestPath = request.getRequestURI();
        String requestMethod = request.getMethod();
        if(requestPath.startsWith((String.format("%s/orders",apiPrefix))) && requestMethod.equals("GET")) {
           if(requestPath.matches(String.format("%s/orders/\\d+",apiPrefix))){
               return true;
           }
           if(requestPath.equals(String.format("%s/orders",apiPrefix))){
               return true;
           }
        }
        for (Pair<String, String> bypassToken : bypassTokens) {
            String tokenPath = bypassToken.getFirst();
            String tokenMethod = bypassToken.getSecond();

            // Kiểm tra nếu tokenPath chứa ** hoặc *
            if (tokenPath.contains("**") || tokenPath.contains("/*")) {
                // Chuyển ** thành .* và /* thành /[^/]* trong regex
                String regexPath = tokenPath.replace("**", ".*").replace("/*", "/[^/]*");

                // Sử dụng Pattern.compile để tạo pattern
                Pattern pattern = Pattern.compile(regexPath);
                Matcher matcher = pattern.matcher(requestPath);

                if (matcher.matches() && requestMethod.equals(tokenMethod)) {
                    return true;
                }
            } else if (requestPath.startsWith(tokenPath) && requestMethod.equals(tokenMethod)) {
                return true;
            }
        }
        return false;
    }
}
