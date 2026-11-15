package com.eldercare360.backend.service;

import com.eldercare360.backend.config.JwtUtil;
import com.eldercare360.backend.dto.AuthResponse;
import com.eldercare360.backend.dto.LoginRequest;
import com.eldercare360.backend.dto.RegisterRequest;
import com.eldercare360.backend.model.AppUser;
import com.eldercare360.backend.model.Role;
import com.eldercare360.backend.repository.AppUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class AuthService {
    private final AppUserRepository appUserRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(AppUserRepository appUserRepository, AuthenticationManager authenticationManager,
                       PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.appUserRepository = appUserRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (appUserRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        }
        AppUser user = new AppUser();
        user.setFullName(request.fullName());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setPhone(request.phone());
        user.setRole(Role.valueOf(request.role().trim().toUpperCase()));
        appUserRepository.save(user);

        return login(new LoginRequest(email, request.password()));
    }

    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.email().trim().toLowerCase(Locale.ROOT), loginRequest.password()));
        User principal = (User) authentication.getPrincipal();
        String token = jwtUtil.generateToken(principal);
        AppUser user = appUserRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return new AuthResponse(token, user.getFullName(), user.getEmail(), user.getRole().name());
    }
}
