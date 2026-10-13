package com.leftoverloop.controller;

import com.leftoverloop.dto.FoodRequestDto;
import com.leftoverloop.dto.RequestActionDto;
import com.leftoverloop.dto.VerifyCollectionDto;
import com.leftoverloop.entity.User;
import com.leftoverloop.service.FoodRequestService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/requests")
public class FoodRequestController {

    @Autowired
    private FoodRequestService requestService;

    @PostMapping
    public ResponseEntity<FoodRequestDto> createRequest(
            @RequestBody Map<String, Object> payload,
            @AuthenticationPrincipal User currentUser) {
        Long donationId = Long.valueOf(payload.get("donationId").toString());
        Integer requestedQuantity = Integer.valueOf(payload.get("requestedQuantity").toString());
        String notes = payload.containsKey("notes") && payload.get("notes") != null ? payload.get("notes").toString() : null;

        return ResponseEntity.ok(requestService.createRequest(donationId, requestedQuantity, notes, currentUser));
    }

    @GetMapping("/my-requests")
    public ResponseEntity<List<FoodRequestDto>> getMyRequests(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(requestService.getRequestsByReceiver(currentUser.getId()));
    }

    @GetMapping("/incoming")
    public ResponseEntity<List<FoodRequestDto>> getIncomingRequests(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(requestService.getIncomingRequestsForUser(currentUser));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FoodRequestDto> getRequestById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(requestService.getRequestById(id, currentUser));
    }

    @GetMapping("/code/{collectionCode}")
    public ResponseEntity<FoodRequestDto> getRequestByCode(
            @PathVariable String collectionCode,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(requestService.getRequestByCode(collectionCode, currentUser));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<FoodRequestDto> updateRequestStatus(
            @PathVariable Long id,
            @Valid @RequestBody RequestActionDto actionDto,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(requestService.updateRequestStatus(id, actionDto, currentUser));
    }

    @PostMapping("/verify")
    public ResponseEntity<FoodRequestDto> verifyCollection(
            @Valid @RequestBody VerifyCollectionDto dto,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(requestService.verifyAndCompleteCollection(dto.getCollectionCode(), currentUser));
    }
}
