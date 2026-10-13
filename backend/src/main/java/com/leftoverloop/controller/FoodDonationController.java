package com.leftoverloop.controller;

import com.leftoverloop.dto.FoodDonationDto;
import com.leftoverloop.entity.User;
import com.leftoverloop.service.FoodDonationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/donations")
public class FoodDonationController {

    @Autowired
    private FoodDonationService donationService;

    @GetMapping("/public/active")
    public ResponseEntity<List<FoodDonationDto>> getActiveDonations(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location) {
        return ResponseEntity.ok(donationService.getAllActiveDonations(category, location));
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<FoodDonationDto> getDonationById(@PathVariable Long id) {
        return ResponseEntity.ok(donationService.getDonationById(id));
    }

    @PostMapping
    public ResponseEntity<FoodDonationDto> createDonation(
            @Valid @RequestBody FoodDonationDto dto,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(donationService.createDonation(dto, currentUser));
    }

    @GetMapping("/my-donations")
    public ResponseEntity<List<FoodDonationDto>> getMyDonations(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(donationService.getDonationsByDonor(currentUser.getId()));
    }

    @GetMapping("/organization")
    public ResponseEntity<List<FoodDonationDto>> getOrganizationDonations() {
        return ResponseEntity.ok(donationService.getOrganizationDonations());
    }

    @PutMapping("/{id}")
    public ResponseEntity<FoodDonationDto> updateDonation(
            @PathVariable Long id,
            @RequestBody FoodDonationDto dto,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(donationService.updateDonation(id, dto, currentUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDonation(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        donationService.deleteDonation(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
