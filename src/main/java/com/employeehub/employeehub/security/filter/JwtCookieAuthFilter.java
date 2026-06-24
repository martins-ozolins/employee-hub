package com.employeehub.employeehub.security.filter;

import com.employeehub.employeehub.config.JwtProperties;
import com.employeehub.employeehub.features.auth.dto.AuthDtos.JwtClaims;
import com.employeehub.employeehub.security.model.AuthenticatedUser;
import com.employeehub.employeehub.security.service.JwtService;
import com.employeehub.employeehub.shared.util.CookieUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtCookieAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final String cookieName;

    public JwtCookieAuthFilter(JwtService jwtService, JwtProperties jwtProperties) {
        this.jwtService = jwtService;
        this.cookieName = jwtProperties.accessCookieName();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String token = CookieUtils.getCookieValue(request, cookieName);
        if (token == null || token.isBlank()) {
            chain.doFilter(request, response);
            return;
        }

        try {
            JwtClaims claims = jwtService.validateJwtAndGetClaims(token);

            AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                    claims.userId(),
                    claims.email(),
                    claims.role()
            );

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    authenticatedUser,
                    null,
                    List.of(new SimpleGrantedAuthority(authenticatedUser.role()))
            );

            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(request, response);
    }
}