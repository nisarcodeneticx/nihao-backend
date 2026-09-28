package com.codeneticx.nihaobackend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Allows the admin HTML file to call the API when opened from disk (file://).
 * Those pages send Origin: null and Chrome may also require Private Network Access.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsConfig extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String origin = request.getHeader("Origin");
        if (!StringUtils.hasText(origin) || "null".equalsIgnoreCase(origin)) {
            response.setHeader("Access-Control-Allow-Origin", "null".equalsIgnoreCase(origin) ? "null" : "*");
        } else {
            response.setHeader("Access-Control-Allow-Origin", origin);
        }
        response.setHeader("Vary", "Origin");
        response.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,PATCH,DELETE,OPTIONS");
        String requestedHeaders = request.getHeader("Access-Control-Request-Headers");
        response.setHeader("Access-Control-Allow-Headers",
                StringUtils.hasText(requestedHeaders)
                        ? requestedHeaders
                        : "Authorization, Content-Type, Accept, Origin");
        response.setHeader("Access-Control-Expose-Headers", "Authorization");
        response.setHeader("Access-Control-Allow-Private-Network", "true");
        response.setHeader("Access-Control-Max-Age", "3600");

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
