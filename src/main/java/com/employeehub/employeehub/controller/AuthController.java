package com.employeehub.employeehub.controller;


import com.employeehub.employeehub.config.AppUserDetails;
import com.employeehub.employeehub.config.JwtProperties;
import com.employeehub.employeehub.dto.ApiResponses.*;
import com.employeehub.employeehub.dto.AuthDtos.*;
import com.employeehub.employeehub.service.AuthService;
import com.employeehub.employeehub.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final String accessCookieName;
    private final String refreshCookieName;

    public AuthController(AuthService authService, JwtProperties jwtProperties) {
        this.authService = authService;
        this.accessCookieName = jwtProperties.accessCookieName();
        this.refreshCookieName = jwtProperties.refreshCookieName();
    }

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody UserRegisterDto dto) {
        authService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Registration successful. Please verify your email before logging in."));
    }

    @PostMapping("/login")
    public ResponseEntity<MessageResponse> login(
            @Valid @RequestBody LoginDto dto,
            HttpServletResponse response
    ) {
        TokenPair tokenPair = authService.login(dto);
        CookieUtils.addCookie(response, accessCookieName, tokenPair.accessToken());
        CookieUtils.addCookie(response, refreshCookieName, tokenPair.refreshToken());
        return ResponseEntity.ok(new MessageResponse("Signed in successfully"));
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(HttpServletRequest request, HttpServletResponse response) {
        String token = CookieUtils.getCookieValue(request, refreshCookieName);
        authService.logout(token);
        response.addHeader("Set-Cookie", accessCookieName + "=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax");
        response.addHeader("Set-Cookie", refreshCookieName + "=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax");
        return ResponseEntity.ok(new MessageResponse("Logged out successfully"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<MessageResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
        String token = CookieUtils.getCookieValue(request, refreshCookieName);
        TokenPair tokenPair = authService.refresh(token);
        CookieUtils.addCookie(response, accessCookieName, tokenPair.accessToken());
        CookieUtils.addCookie(response, refreshCookieName, tokenPair.refreshToken());
        return ResponseEntity.ok(new MessageResponse("Token refreshed"));
    }

    @GetMapping("verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(@RequestParam UUID token) {
        authService.verifyEmail(token);

        return ResponseEntity.status(HttpStatus.OK).body(new MessageResponse("Email verified successfully"));
    }

    @PostMapping("forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@RequestBody @Valid ForgotPasswordDto dto) {
        authService.forgotPassword(dto);

        return ResponseEntity.status(HttpStatus.OK).body(new MessageResponse("If an account with that email exists, a password reset link has been sent."));
    }

    @PostMapping("reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@RequestParam UUID token, @RequestBody @Valid ResetPasswordDto dto) {
        authService.resetPassword(token, dto);

        return ResponseEntity.status(HttpStatus.OK).body(new MessageResponse("Password reset successfully."));
    }

    @PostMapping("change-password")
    public ResponseEntity<MessageResponse> changePassword(@AuthenticationPrincipal AppUserDetails principal, @RequestBody @Valid ChangePasswordDto dto) {
        authService.changePassword(principal, dto);

        return ResponseEntity.status(HttpStatus.OK).body(new MessageResponse("Password changed successfully."));
    }

}