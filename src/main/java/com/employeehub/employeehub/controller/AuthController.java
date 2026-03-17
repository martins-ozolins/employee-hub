package com.employeehub.employeehub.controller;


import com.employeehub.employeehub.dto.ApiResponse;
import com.employeehub.employeehub.dto.AuthDtos;
import com.employeehub.employeehub.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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

    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody AuthDtos.UserRegisterDto dto) {

        authService.register(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>("User registered", null));

    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(
            @Valid @RequestBody AuthDtos.LoginDto dto,
            HttpServletResponse response
    ) {
        AuthDtos.TokenPair tokenPair = authService.login(dto);
        issueCookie(response, tokenPair.accessToken(), "access_token");
        issueCookie(response, tokenPair.refreshToken(), "refresh_token");

        return ResponseEntity.ok(new ApiResponse("Signed in successfully"));
    }

    private void issueCookie(HttpServletResponse response, String jwt, String cookieName) {
        response.addHeader(
                "Set-Cookie",
                cookieName + "=" + jwt + "; Path=/; HttpOnly; SameSite=Lax"
        );
        // For production HTTPS add: "; Secure"
    }

}
