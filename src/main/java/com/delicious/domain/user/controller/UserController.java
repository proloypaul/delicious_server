package com.delicious.domain.user.controller;

import com.delicious.common.response.ApiResponse;
import com.delicious.domain.user.entity.User;
import com.delicious.domain.user.enums.UserStatus;
import com.delicious.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PutMapping("/{id}/status")
    @Operation(summary = "Update user status (ACTIVE/INACTIVE)")
    public ResponseEntity<ApiResponse<User>> updateStatus(
            @PathVariable Long id,
            @RequestParam UserStatus status) {
        User user = userService.updateUserStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(user, "User status updated successfully to " + status));
    }
}
