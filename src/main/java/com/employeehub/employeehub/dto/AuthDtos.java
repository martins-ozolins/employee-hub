package com.employeehub.employeehub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.aspectj.weaver.ast.Not;

import java.time.Instant;
import java.util.UUID;

public class AuthDtos {

    public record UserRegisterDto(
            @Email @NotBlank String email,
            @NotBlank String firstName,
            @NotBlank String lastName,
            String middleName,
            @NotBlank String password
    ) {}

    public record LoginDto(
            @Email @NotBlank String email,
            @NotBlank String password
    ) {}

    public record JwtClaims(String email, UUID userId, UUID jti){}

    public record RefreshTokenResult(String token, UUID jti, Instant expiresAt) { }

    public record TokenPair(String accessToken, String refreshToken) {}

    public record ForgotPasswordDto(
            @Email @NotBlank String email
    ) {}

    public record ResetPasswordDto(
            @NotBlank String password,
            @NotBlank String passwordConfirmation
    ) {}

}
