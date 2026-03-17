package com.employeehub.employeehub.service;


import com.employeehub.employeehub.dto.AuthDtos.*;
import com.employeehub.employeehub.entity.PlatformRole;
import com.employeehub.employeehub.entity.RefreshToken;
import com.employeehub.employeehub.entity.User;
import com.employeehub.employeehub.exception.EmailAlreadyUsedException;
import com.employeehub.employeehub.exception.InvalidCredentialsException;
import com.employeehub.employeehub.repository.RefreshTokenRepository;
import com.employeehub.employeehub.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;


    public AuthService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }


    public void register(UserRegisterDto dto)  {

        if (userRepository.existsByEmail(dto.email())) {
            throw new EmailAlreadyUsedException();
        }

        User u = new User();
        u.setEmail(dto.email());
        u.setFirstName(dto.firstName());
        u.setLastName(dto.lastName());
        u.setPasswordHash(passwordEncoder.encode(dto.password()));
        u.setRole(PlatformRole.USER);

        userRepository.save(u);
    }

    public TokenPair login(LoginDto dto) {
        User user = userRepository.findByEmail(dto.email())
                .orElseThrow(InvalidCredentialsException::new);

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



}
