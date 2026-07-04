package com.employeehub.employeehub.features.auth.service;

import com.employeehub.employeehub.features.auth.dto.AuthDtos.*;
import com.employeehub.employeehub.features.auth.entity.PlatformRole;
import com.employeehub.employeehub.features.auth.entity.RefreshToken;
import com.employeehub.employeehub.features.auth.entity.TokenType;
import com.employeehub.employeehub.features.auth.entity.User;
import com.employeehub.employeehub.features.auth.repository.RefreshTokenRepository;
import com.employeehub.employeehub.features.auth.repository.UserRepository;
import com.employeehub.employeehub.features.email.event.EmailEvent;
import com.employeehub.employeehub.features.email.event.EmailEventPublisher;
import com.employeehub.employeehub.features.email.event.EmailEventType;
import com.employeehub.employeehub.features.members.entity.CompanyMember;
import com.employeehub.employeehub.features.members.repository.CompanyMemberRepository;
import com.employeehub.employeehub.security.model.AuthenticatedUser;
import com.employeehub.employeehub.security.service.JwtService;
import com.employeehub.employeehub.shared.exception.BadRequestException;
import com.employeehub.employeehub.shared.exception.EmailAlreadyUsedException;
import com.employeehub.employeehub.shared.exception.EmailNotVerifiedException;
import com.employeehub.employeehub.shared.exception.InvalidCredentialsException;
import jakarta.validation.Valid;
import com.employeehub.employeehub.config.AppProperties;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CompanyMemberRepository companyMemberRepository;
    private final VerificationTokenService verificationTokenService;
    private final String baseUrl;
    private final EmailEventPublisher emailEventPublisher;


    public AuthService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository, PasswordEncoder passwordEncoder, JwtService jwtService, CompanyMemberRepository companyMemberRepository, VerificationTokenService verificationTokenService, AppProperties appProperties, EmailEventPublisher emailEventPublisher) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.companyMemberRepository = companyMemberRepository;
        this.verificationTokenService = verificationTokenService;
        this.baseUrl = appProperties.baseUrl();
        this.emailEventPublisher = emailEventPublisher;
    }


    @Transactional
    public void register(UserRegisterDto dto)  {

        if (userRepository.existsByEmail(dto.email())) {
            throw new EmailAlreadyUsedException();
        }

        User u = new User();
        u.setEmail(dto.email());
        u.setFirstName(dto.firstName());
        u.setLastName(dto.lastName());
        u.setMiddleName(dto.middleName());
        u.setPasswordHash(passwordEncoder.encode(dto.password()));
        u.setRole(PlatformRole.USER);
        u.setIsActive(true);
        u.setEmailVerified(false);

        User savedUser = userRepository.save(u);

        List<CompanyMember> pending = companyMemberRepository.findByPersonalEmailAndUserIsNull(dto.email());
        for (CompanyMember member : pending) {
            member.setUser(savedUser);
            companyMemberRepository.save(member);
        }

        UUID token = verificationTokenService.generateToken(savedUser, TokenType.EMAIL_VERIFICATION);

        Map<String, String> params = new HashMap<>();
        params.put("token", token.toString());
        params.put("baseUrl", baseUrl);
        emailEventPublisher.publish(new EmailEvent(EmailEventType.EMAIL_VERIFICATION, savedUser.getEmail(), params));
    }

    public TokenPair login(LoginDto dto) {
        User user = userRepository.findByEmail(dto.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!user.getIsActive()) throw new InvalidCredentialsException();

        if (!passwordEncoder.matches(dto.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        if (!user.getEmailVerified()) {

            UUID token = verificationTokenService.generateToken(user, TokenType.EMAIL_VERIFICATION);

            Map<String, String> params = new HashMap<>();
            params.put("token", token.toString());
            params.put("baseUrl", baseUrl);
            emailEventPublisher.publish(new EmailEvent(EmailEventType.EMAIL_VERIFICATION, user.getEmail(), params));

            throw new EmailNotVerifiedException();
        }

        String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getId(), user.getRole().name());
        RefreshTokenResult refreshTokenResult = jwtService.generateRefreshToken(user.getEmail(), user.getId(), user.getRole().name());

        RefreshToken entity = new RefreshToken();
        entity.setJti(refreshTokenResult.jti());
        entity.setOwner(user);
        entity.setExpiresAt(refreshTokenResult.expiresAt());
        refreshTokenRepository.save(entity);

        return new TokenPair(accessToken, refreshTokenResult.token());
    }

    public void logout(String token) {
        try {
            JwtClaims claims = jwtService.validateJwtAndGetClaims(token);
            refreshTokenRepository.findByJti(claims.jti()).ifPresent(t -> {
                t.setRevoked(true);
                refreshTokenRepository.save(t);
            });
        } catch (Exception ignored) {
            // token invalid or already expired — nothing to revoke
        }
    }

    public TokenPair refresh(String token) {
        JwtClaims claims;
        try {
            claims = jwtService.validateJwtAndGetClaims(token);
        } catch (Exception e) {
            throw new InvalidCredentialsException();
        }

        RefreshToken refreshToken = refreshTokenRepository.findByJti(claims.jti())
                .orElseThrow(InvalidCredentialsException::new);

        if (refreshToken.getRevoked()) {
            throw new InvalidCredentialsException();
        }

        // rotate: revoke old token
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        // issue new token pair
        User owner = refreshToken.getOwner();
        String newAccessToken = jwtService.generateAccessToken(owner.getEmail(), owner.getId(), owner.getRole().name());
        RefreshTokenResult newRefreshResult = jwtService.generateRefreshToken(owner.getEmail(), owner.getId(), owner.getRole().name());

        RefreshToken newEntity = new RefreshToken();
        newEntity.setJti(newRefreshResult.jti());
        newEntity.setOwner(owner);
        newEntity.setExpiresAt(newRefreshResult.expiresAt());
        refreshTokenRepository.save(newEntity);

        return new TokenPair(newAccessToken, newRefreshResult.token());
    }

    @Transactional
    public void verifyEmail(UUID token) {
        User user = verificationTokenService.verifyToken(token, TokenType.EMAIL_VERIFICATION);
        user.setEmailVerified(true);
    }

    public void forgotPassword(ForgotPasswordDto dto) {

        Optional<User> user = userRepository.findByEmail(dto.email());

        if (user.isPresent()) {
            UUID token = verificationTokenService.generateToken(user.get(), TokenType.PASSWORD_RESET);

            Map<String, String> params = new HashMap<>();
            params.put("token", token.toString());
            params.put("baseUrl", baseUrl);
            emailEventPublisher.publish(new EmailEvent(EmailEventType.PASSWORD_RESET_REQUESTED, user.get().getEmail(), params));

        }

    }

    @Transactional
    public void resetPassword(UUID token, ResetPasswordDto dto) {
        if (!dto.password().equals(dto.passwordConfirmation())) {
            throw new BadRequestException("Passwords do not match.");
        }

        User user = verificationTokenService.verifyToken(token, TokenType.PASSWORD_RESET);
        user.setPasswordHash(passwordEncoder.encode(dto.password()));

        emailEventPublisher.publish(new EmailEvent(EmailEventType.PASSWORD_RESET, user.getEmail(), Map.of()));

    }

    public void changePassword(AuthenticatedUser principal, @Valid ChangePasswordDto dto) {

        User user = userRepository.findById(principal.id()).orElseThrow(() -> new InvalidCredentialsException());

        if (!passwordEncoder.matches(dto.currentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password does not match.");
        }

        if (!dto.newPassword().equals(dto.passwordConfirmation())) {
            throw new BadRequestException("Passwords do not match.");
        }

        user.setPasswordHash(passwordEncoder.encode(dto.newPassword()));

        emailEventPublisher.publish(new EmailEvent(EmailEventType.PASSWORD_CHANGED, user.getEmail(), Map.of()));

    }
}
