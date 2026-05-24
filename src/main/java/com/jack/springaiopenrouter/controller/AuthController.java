package com.jack.springaiopenrouter.controller;

import com.jack.springaiopenrouter.dto.AuthResponse;
import com.jack.springaiopenrouter.dto.LoginRequest;
import com.jack.springaiopenrouter.dto.RegisterRequest;
import com.jack.springaiopenrouter.dto.UserProfileResponse;
import com.jack.springaiopenrouter.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UserProfileResponse me(Authentication authentication) {
        return authService.currentUser(authentication.getName());
    }
}
