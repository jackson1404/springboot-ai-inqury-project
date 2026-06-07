package com.jack.springaiopenrouter.service;

import com.jack.springaiopenrouter.dto.AuthResponse;
import com.jack.springaiopenrouter.dto.LoginRequest;
import com.jack.springaiopenrouter.dto.RegisterRequest;
import com.jack.springaiopenrouter.dto.UserProfileResponse;
import com.jack.springaiopenrouter.entity.UserEntity;
import com.jack.springaiopenrouter.entity.UserRole;
import com.jack.springaiopenrouter.repository.UserRepository;
import com.jack.springaiopenrouter.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email is already registered");
        }

        UserEntity user = new UserEntity(
                email,
                request.displayName().trim(),
                passwordEncoder.encode(request.password()),
                UserRole.USER
        );
        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        return buildAuthResponse(user, userDetails);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.password())
            );
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("Invalid email or password");
        }

        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        return buildAuthResponse(user, userDetails);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse currentUser(String email) {
        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
        return toProfile(user);
    }

    private AuthResponse buildAuthResponse(UserEntity user, UserDetails userDetails) {
        Instant expiresAt = jwtService.expiresAtFromNow();
        String token = jwtService.generateToken(
                userDetails,
                Map.of(
                        "userId", user.getId(),
                        "role", user.getRole().name(),
                        "displayName", user.getDisplayName()
                )
        );
        return new AuthResponse("Bearer", token, expiresAt, toProfile(user));
    }

    private UserProfileResponse toProfile(UserEntity user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getRole().name(),
                user.getCreatedAt()
        );
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
