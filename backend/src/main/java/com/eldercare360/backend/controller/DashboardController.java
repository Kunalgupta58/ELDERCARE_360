package com.eldercare360.backend.controller;

import com.eldercare360.backend.dto.DashboardSummary;
import com.eldercare360.backend.model.AppUser;
import com.eldercare360.backend.service.DashboardService;
import com.eldercare360.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DashboardController {
    private final DashboardService dashboardService;
    private final UserService userService;

    public DashboardController(DashboardService dashboardService, UserService userService) {
        this.dashboardService = dashboardService;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardSummary> getDashboard() {
        AppUser user = userService.getCurrentUser();
        return ResponseEntity.ok(dashboardService.buildForUser(user));
    }
}
