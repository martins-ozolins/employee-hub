package com.employeehub.employeehub.security;

import com.employeehub.employeehub.dto.AuthDtos;
import com.employeehub.employeehub.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
public class JwtCookieAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final String cookieName;

    public JwtCookieAuthFilter(
            JwtService jwtService,
            UserDetailsService userDetailsService,
            @Value("${app.jwt.cookieName:access_token}") String cookieName
    ) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.cookieName = cookieName;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // If already authenticated, skip
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            chain.doFilter(request, response);
            return;
        }

        String token = getCookieValue(request, cookieName);
        if (token == null || token.isBlank()) {
            chain.doFilter(request, response);
            return;
        }

        try {
            AuthDtos.JwtClaims jwtClaims = jwtService.validateJwtAndGetClaims(token);

            UserDetails userDetails = userDetailsService.loadUserByUsername(jwtClaims.email());

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (Exception ex) {
            // Token invalid/expired → treat as unauthenticated
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(request, response);
    }

    private String getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        return Arrays.stream(cookies)
                .filter(c -> name.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}