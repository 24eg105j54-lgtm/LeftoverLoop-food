package com.leftoverloop.controller;

import com.leftoverloop.dto.FoodDonationDto;
import com.leftoverloop.dto.FoodRequestDto;
import com.leftoverloop.dto.UserDto;
import com.leftoverloop.entity.Role;
import com.leftoverloop.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PatchMapping("/users/{userId}/toggle-status")
    public ResponseEntity<UserDto> toggleUserStatus(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.toggleUserStatus(userId));
    }

    @PatchMapping("/users/{userId}/role")
    public ResponseEntity<UserDto> updateUserRole(
            @PathVariable Long userId,
            @RequestBody Map<String, String> payload) {
        Role role = Role.valueOf(payload.get("role"));
        return ResponseEntity.ok(adminService.updateUserRole(userId, role));
    }

    @GetMapping("/donations")
    public ResponseEntity<List<FoodDonationDto>> getAllDonations() {
        return ResponseEntity.ok(adminService.getAllDonations());
    }

    @GetMapping("/requests")
    public ResponseEntity<List<FoodRequestDto>> getAllRequests() {
        return ResponseEntity.ok(adminService.getAllRequests());
    }
}
