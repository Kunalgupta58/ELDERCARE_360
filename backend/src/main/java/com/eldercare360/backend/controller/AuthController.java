package com.eldercare360.backend.controller;

import com.eldercare360.backend.dto.AuthResponse;
import com.eldercare360.backend.dto.LoginRequest;
import com.eldercare360.backend.dto.RegisterRequest;
import com.eldercare360.backend.dto.UserSummary;
import com.eldercare360.backend.model.AppUser;
import com.eldercare360.backend.service.AuthService;
import com.eldercare360.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserSummary> getCurrentUser(Authentication authentication) {
        AppUser user = userService.getCurrentUser();
        return ResponseEntity.ok(userService.toSummary(user));
    }
}
