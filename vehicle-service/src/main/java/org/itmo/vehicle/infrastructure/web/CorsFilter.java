package org.itmo.vehicle.infrastructure.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * CORS для web-client, открытого с se.ifmo.ru. Заголовки ставятся
 * только для origins из soa.cors.allowed-origins; остальные — без CORS,
 * браузер сам не даст странице читать ответ.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsFilter extends OncePerRequestFilter {

    private final Set<String> allowedOrigins;

    public CorsFilter(@Value("${soa.cors.allowed-origins:}") Set<String> allowedOrigins) {
        this.allowedOrigins = Set.copyOf(allowedOrigins);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String origin = request.getHeader("Origin");
        boolean allowed = origin != null && allowedOrigins.contains(origin);
        boolean preflight = "OPTIONS".equals(request.getMethod())
                && request.getHeader("Access-Control-Request-Method") != null;

        if (allowed) {
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.addHeader("Vary", "Origin");
            response.setHeader("Access-Control-Expose-Headers", "Location");
        }
        if (preflight) {
            if (allowed) {
                response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
                response.setHeader("Access-Control-Allow-Headers", "Content-Type");
                response.setHeader("Access-Control-Max-Age", "3600");
            }
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }
        chain.doFilter(request, response);
    }
}
