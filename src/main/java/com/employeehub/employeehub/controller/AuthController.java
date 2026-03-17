package com.employeehub.employeehub.controller;


import com.employeehub.employeehub.dto.ApiResponse;
import com.employeehub.employeehub.dto.AuthDtos.*;
import com.employeehub.employeehub.service.AuthService;
import com.employeehub.employeehub.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final String accessCookieName;
    private final String refreshCookieName;

    public AuthController(
            AuthService authService,
            @Value("${app.jwt.accessCookieName}") String accessCookieName,
            @Value("${app.jwt.refreshCookieName}") String refreshCookieName
    ) {
        this.authService = authService;
        this.accessCookieName = accessCookieName;
        this.refreshCookieName = refreshCookieName;
    }


    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody UserRegisterDto dto) {

        authService.register(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>("User registered", null));

    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(
            @Valid @RequestBody LoginDto dto,
            HttpServletResponse response
    ) {
        TokenPair tokenPair = authService.login(dto);
        CookieUtils.addCookie(response, accessCookieName, tokenPair.accessToken());
        CookieUtils.addCookie(response, refreshCookieName, tokenPair.refreshToken());

        return ResponseEntity.ok(new ApiResponse("Signed in successfully"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(HttpServletRequest request, HttpServletResponse response) {

        String token = CookieUtils.getCookieValue(request, refreshCookieName);

        authService.logout(token);

        response.addHeader(
                "Set-Cookie",
                accessCookieName + "=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax"
        );
        response.addHeader(
                "Set-Cookie",
                refreshCookieName + "=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax"
        );
        return ResponseEntity.ok(new ApiResponse("Logged out successfully"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
        String token = CookieUtils.getCookieValue(request, refreshCookieName);

        TokenPair tokenPair = authService.refresh(token);

        CookieUtils.addCookie(response, accessCookieName, tokenPair.accessToken());
        CookieUtils.addCookie(response, refreshCookieName, tokenPair.refreshToken());

        return ResponseEntity.ok(new ApiResponse("Token refreshed"));
    }

}
