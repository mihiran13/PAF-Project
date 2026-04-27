package com.smartcampus.controller;

import com.smartcampus.dto.request.LoginRequest;
import com.smartcampus.dto.request.RegisterRequest;
import com.smartcampus.dto.response.ApiResponse;
import com.smartcampus.dto.response.AuthResponse;
import com.smartcampus.dto.response.UserResponse;
import com.smartcampus.enums.UserRole;
import com.smartcampus.model.User;
import com.smartcampus.repository.UserRepository;
import com.smartcampus.security.CurrentUser;
import com.smartcampus.security.CustomUserDetails;
import com.smartcampus.security.JwtTokenProvider;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String jwt = tokenProvider.generateToken(user);

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(jwt)
                .tokenType("Bearer")
                .user(UserResponse.fromEntity(user))
                .build();

        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Email is already in use!"));
        }

        User user = User.builder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(UserRole.USER)
                .isActive(true)
                .build();

        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("User registered successfully", null));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@CurrentUser CustomUserDetails currentUser) {
        User user = currentUser.getUser();
        UserResponse response = UserResponse.fromEntity(user);
        return ResponseEntity.ok(ApiResponse.success("Current user retrieved", response));
    }

    @PostMapping("/google-login")
    public ResponseEntity<ApiResponse<AuthResponse>> googleLogin(@RequestBody Map<String, String> request) {
        String googleToken = request.get("token");
        
        if (googleToken == null || googleToken.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Google token is required"));
        }

        // In production, validate the Google token with Google API
        // For now, extract email from token claim (basic implementation)
        // Note: This is a simplified implementation. In production, use Google's TokenVerifier
        
        try {
            // Extract email from Google JWT (basic decode, not validated against Google)
            // You should integrate with Google's TokenVerifier in production
            String email = extractEmailFromGoogleToken(googleToken);
            
            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> {
                        // Create new user if doesn't exist
                        User newUser = User.builder()
                                .email(email)
                                .name(email.split("@")[0])
                                .role(UserRole.USER)
                                .isActive(true)
                                .oauthId(email)
                                .build();
                        return userRepository.save(newUser);
                    });

            String jwt = tokenProvider.generateToken(user);
            
            AuthResponse authResponse = AuthResponse.builder()
                    .accessToken(jwt)
                    .tokenType("Bearer")
                    .user(UserResponse.fromEntity(user))
                    .build();

            return ResponseEntity.ok(ApiResponse.success("Google login successful", authResponse));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Invalid Google token: " + e.getMessage()));
        }
    }

    private String extractEmailFromGoogleToken(String token) {
        // Basic implementation: split JWT and decode payload
        // In production, use proper JWT library and validate signature with Google's public key
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new RuntimeException("Invalid JWT format");
            }
            
            // Decode payload (basic base64 decode)
            byte[] decoded = java.util.Base64.getUrlDecoder().decode(parts[1]);
            String payload = new String(decoded);
            
            // Extract email using simple string parsing (not ideal)
            // In production, use a JSON parser
            if (payload.contains("\"email\":\"")) {
                int start = payload.indexOf("\"email\":\"") + 9;
                int end = payload.indexOf("\"", start);
                return payload.substring(start, end);
            }
            
            throw new RuntimeException("Email not found in token");
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract email from token: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }
}
