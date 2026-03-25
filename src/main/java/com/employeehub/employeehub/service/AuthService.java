package com.employeehub.employeehub.service;


import com.employeehub.employeehub.dto.AuthDtos.*;
import com.employeehub.employeehub.entity.*;
import com.employeehub.employeehub.exception.EmailAlreadyUsedException;
import com.employeehub.employeehub.exception.EmailNotVerifiedException;
import com.employeehub.employeehub.exception.InvalidCredentialsException;
import com.employeehub.employeehub.repository.CompanyMemberRepository;
import com.employeehub.employeehub.repository.RefreshTokenRepository;
import com.employeehub.employeehub.repository.UserRepository;
import com.employeehub.employeehub.service.email.EmailSender;
import com.employeehub.employeehub.service.email.EmailTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CompanyMemberRepository companyMemberRepository;
    private final EmailSender emailSender;
    private final VerificationTokenService verificationTokenService;


    public AuthService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository, PasswordEncoder passwordEncoder, JwtService jwtService, CompanyMemberRepository companyMemberRepository, EmailSender emailSender, VerificationTokenService verificationTokenService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.companyMemberRepository = companyMemberRepository;
        this.emailSender = emailSender;
        this.verificationTokenService = verificationTokenService;
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

        emailSender.send(savedUser.getEmail(), EmailTemplate.EMAIL_VERIFICATION, token);

    }

    public TokenPair login(LoginDto dto) {
        User user = userRepository.findByEmail(dto.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!user.getIsActive()) throw new InvalidCredentialsException();

        if (!user.getEmailVerified()) {

            UUID token = verificationTokenService.generateToken(user, TokenType.EMAIL_VERIFICATION);

            emailSender.send(user.getEmail(), EmailTemplate.EMAIL_VERIFICATION, token);

            throw new EmailNotVerifiedException();
        }

        if (!passwordEncoder.matches(dto.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getId());
        RefreshTokenResult refreshTokenResult = jwtService.generateRefreshToken(user.getEmail(), user.getId());

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
        String newAccessToken = jwtService.generateAccessToken(owner.getEmail(), owner.getId());
        RefreshTokenResult newRefreshResult = jwtService.generateRefreshToken(owner.getEmail(), owner.getId());

        RefreshToken newEntity = new RefreshToken();
        newEntity.setJti(newRefreshResult.jti());
        newEntity.setOwner(owner);
        newEntity.setExpiresAt(newRefreshResult.expiresAt());
        refreshTokenRepository.save(newEntity);

        return new TokenPair(newAccessToken, newRefreshResult.token());
    }

    public void verifyEmail(UUID token) {
        verificationTokenService.verifyToken(token, TokenType.EMAIL_VERIFICATION);
    }

}
