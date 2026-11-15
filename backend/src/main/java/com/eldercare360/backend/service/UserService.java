package com.eldercare360.backend.service;

import com.eldercare360.backend.dto.UserSummary;
import com.eldercare360.backend.model.AppUser;
import com.eldercare360.backend.model.Role;
import com.eldercare360.backend.repository.AppUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final AppUserRepository appUserRepository;

    public UserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public AppUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            throw new IllegalStateException("No authenticated user");
        }
        return appUserRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));
    }

    public UserSummary toSummary(AppUser appUser) {
        return new UserSummary(appUser.getId(), appUser.getFullName(), appUser.getEmail(), appUser.getPhone(), appUser.getRole().name());
    }

    public Role parseRole(String role) {
        return Role.valueOf(role.trim().toUpperCase());
    }
}
