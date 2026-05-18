package com.delicious.domain.admin.controller;

import com.delicious.common.response.ApiResponse;
import com.delicious.domain.admin.dto.AdminStatsResponse;
import com.delicious.domain.admin.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Management", description = "Endpoints for platform administration operations and system metrics")
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "Get Dashboard Stats", description = "Retrieves platform-wide counters, performance indicators and total revenue metrics.")
    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<AdminStatsResponse>> getDashboardStats() {
        AdminStatsResponse stats = adminService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(stats, "Dashboard statistics retrieved successfully"));
    }
}
