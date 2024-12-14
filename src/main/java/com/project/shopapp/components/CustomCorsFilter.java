package com.project.shopapp.components;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CustomCorsFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String allowedOrigin = "http://localhost:4200"; // Frontend domain
        response.setHeader("Access-Control-Allow-Origin", allowedOrigin);
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, xsrf-token, x-requested-with,Origin");
        response.addHeader("Access-Control-Expose-Headers", "xsrf-token");

        try {
            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                response.setStatus(HttpServletResponse.SC_OK);
                return;
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            // Đảm bảo header vẫn được gửi kèm khi có lỗi
            response.setHeader("Access-Control-Allow-Origin", allowedOrigin);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        }
    }
}


