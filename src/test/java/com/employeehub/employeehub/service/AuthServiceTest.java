package com.employeehub.employeehub.service;

import com.employeehub.employeehub.dto.AuthDtos.*;
import com.employeehub.employeehub.entity.TokenType;
import com.employeehub.employeehub.entity.User;
import com.employeehub.employeehub.event.EmailEvent;
import com.employeehub.employeehub.event.EmailEventPublisher;
import com.employeehub.employeehub.event.EmailEventType;
import com.employeehub.employeehub.exception.EmailAlreadyUsedException;
import com.employeehub.employeehub.repository.CompanyMemberRepository;
import com.employeehub.employeehub.repository.RefreshTokenRepository;
import com.employeehub.employeehub.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    // These are mocked because AuthService depends on them,
    // but this test is not testing their real implementations.
    @Mock
    UserRepository userRepository;

    @Mock
    RefreshTokenRepository refreshTokenRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    JwtService jwtService;

    @Mock
    CompanyMemberRepository companyMemberRepository;

    @Mock
    VerificationTokenService verificationTokenService;

    @Mock
    EmailEventPublisher emailEventPublisher;

    // Real instance of the class we are testing.
    // It receives mocked dependencies so we can test AuthService logic in isolation.
    AuthService authService;

    @BeforeEach
    void setUp() {
        // Create AuthService manually because this is a unit test, not a Spring context test.
        // The baseUrl is normally injected from application properties, so we pass it manually here.
        authService = new AuthService(
                userRepository,
                refreshTokenRepository,
                passwordEncoder,
                jwtService,
                companyMemberRepository,
                verificationTokenService,
                "http://localhost:8080",
                emailEventPublisher
        );
    }

    @Test
    void register_whenEmailNotUsed_createsUserAndSendsVerificationEmail() {
        // Arrange: create input data for the method we want to test.
        UserRegisterDto dto = new UserRegisterDto(
                "user@test.com",
                "User",
                "Test",
                "",
                "test12345"
        );

        UUID token = UUID.randomUUID();

        // Arrange: tell mocked dependencies what to return when AuthService calls them.
        // In this scenario, the email is available, so registration should continue.
        when(userRepository.existsByEmail(dto.email())).thenReturn(false);

        // AuthService should encode the raw password before saving the user.
        when(passwordEncoder.encode(dto.password())).thenReturn("encoded-password");

        // Simulate repository.save(...) returning the saved User.
        // We return the same User object that AuthService passes into save(...).
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return user;
        });

        // Simulate that there are no pending company memberships linked to this email.
        when(companyMemberRepository.findByPersonalEmailAndUserIsNull(dto.email()))
                .thenReturn(List.of());

        // Simulate token generation for email verification.
        when(verificationTokenService.generateToken(any(User.class), eq(TokenType.EMAIL_VERIFICATION)))
                .thenReturn(token);

        // Act: call the real method we are testing.
        authService.register(dto);

        // Assert/Verify: check that AuthService called the expected dependencies.
        verify(userRepository).existsByEmail("user@test.com");
        verify(passwordEncoder).encode("test12345");
        verify(userRepository).save(any(User.class));
        verify(verificationTokenService).generateToken(any(User.class), eq(TokenType.EMAIL_VERIFICATION));

        // Create a captor for EmailEvent arguments.
        // AuthService creates a new EmailEvent inside register(...), so we cannot reference
        // that exact instance directly in the test.
        //
        // Mockito has already recorded that emailEventPublisher.publish(...) was called.
        // Here we verify that publish(...) was called and capture the EmailEvent instance
        // that AuthService passed into it.
        ArgumentCaptor<EmailEvent> eventCaptor = ArgumentCaptor.forClass(EmailEvent.class);
        verify(emailEventPublisher).publish(eventCaptor.capture());

        // Now we can inspect the actual EmailEvent created by AuthService.
        EmailEvent event = eventCaptor.getValue();

        // Assert: verify the published email event contains the expected verification data.
        assertThat(event.getType()).isEqualTo(EmailEventType.EMAIL_VERIFICATION);
        assertThat(event.getRecipientEmail()).isEqualTo("user@test.com");
        assertThat(event.getData().get("token")).isEqualTo(token.toString());
        assertThat(event.getData().get("baseUrl")).isEqualTo("http://localhost:8080");
    }

    @Test
    void register_whenEmailAlreadyUsed_throwsException() {
        // Arrange: create input data for the method we want to test.
        UserRegisterDto dto = new UserRegisterDto(
                "user@test.com",
                "User",
                "Test",
                "",
                "test12345"
        );

        // Arrange: simulate that this email already exists in the database.
        when(userRepository.existsByEmail(dto.email())).thenReturn(true);

        // Act + Assert: registration should fail with EmailAlreadyUsedException.
        assertThatThrownBy(() -> authService.register(dto))
                .isInstanceOf(EmailAlreadyUsedException.class);

        // Verify: because registration failed early, no user should be saved and no email should be sent.
        verify(userRepository, never()).save(any());
        verify(emailEventPublisher, never()).publish(any());
    }
}