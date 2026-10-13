package com.leftoverloop.service;

import com.leftoverloop.dto.FoodRequestDto;
import com.leftoverloop.dto.RequestActionDto;
import com.leftoverloop.entity.*;
import com.leftoverloop.exception.BadRequestException;
import com.leftoverloop.exception.ResourceNotFoundException;
import com.leftoverloop.exception.UnauthorizedException;
import com.leftoverloop.repository.FoodDonationRepository;
import com.leftoverloop.repository.FoodRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FoodRequestService {

    @Autowired
    private FoodRequestRepository requestRepository;

    @Autowired
    private FoodDonationRepository donationRepository;

    @Autowired
    private FoodDonationService donationService;

    @Transactional
    public FoodRequestDto createRequest(Long donationId, Integer requestedQuantity, String notes, User currentReceiver) {
        if (currentReceiver.getRole() != Role.ROLE_RECEIVER && currentReceiver.getRole() != Role.ROLE_ORGANIZATION && currentReceiver.getRole() != Role.ROLE_ADMIN) {
            throw new UnauthorizedException("Only registered receivers, organizations, or admins can request food");
        }

        FoodDonation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new ResourceNotFoundException("Food donation not found with ID: " + donationId));

        if (donation.getExpiryDate() != null && donation.getExpiryDate().isBefore(LocalDateTime.now())) {
            donation.setStatus(DonationStatus.EXPIRED);
            donationRepository.save(donation);
            throw new BadRequestException("Cannot request expired food donation");
        }

        if (donation.getStatus() == DonationStatus.EXPIRED || donation.getStatus() == DonationStatus.CANCELLED) {
            throw new BadRequestException("This food donation is no longer available");
        }

        if (requestedQuantity == null || requestedQuantity <= 0) {
            throw new BadRequestException("Requested quantity must be at least 1");
        }

        // Concurrency-safe remaining quantity check
        Integer remainingQuantity = donationService.calculateRemainingQuantity(donation.getId(), donation.getInitialQuantity());
        if (requestedQuantity > remainingQuantity) {
            throw new BadRequestException("Requested quantity (" + requestedQuantity + ") exceeds available remaining quantity (" + remainingQuantity + ")");
        }

        FoodRequest request = new FoodRequest();
        request.setDonation(donation);
        request.setReceiver(currentReceiver);
        request.setRequestedQuantity(requestedQuantity);
        request.setStatus(RequestStatus.PENDING);
        request.setNotes(notes);

        // Generate unique collection code
        String code = "LL-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        request.setCollectionCode(code);

        FoodRequest savedRequest = requestRepository.save(request);

        // Recalculate donation status (e.g. AVAILABLE -> PARTIALLY_RESERVED or RESERVED)
        donationService.recalculateDonationStatus(donation.getId());

        return new FoodRequestDto(savedRequest);
    }

    public List<FoodRequestDto> getRequestsByReceiver(Long receiverId) {
        return requestRepository.findByReceiverIdOrderByRequestedAtDesc(receiverId).stream()
                .map(FoodRequestDto::new)
                .collect(Collectors.toList());
    }

    public List<FoodRequestDto> getIncomingRequestsForUser(User currentUser) {
        return requestRepository.findIncomingRequestsForDonor(currentUser.getId()).stream()
                .map(FoodRequestDto::new)
                .collect(Collectors.toList());
    }

    public FoodRequestDto getRequestById(Long requestId, User currentUser) {
        FoodRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Food request not found with ID: " + requestId));

        boolean isReceiver = request.getReceiver().getId().equals(currentUser.getId());
        boolean isDonor = request.getDonation().getDonor().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ROLE_ADMIN;

        if (!isReceiver && !isDonor && !isAdmin) {
            throw new UnauthorizedException("You are not authorized to view this request");
        }

        return new FoodRequestDto(request);
    }

    public FoodRequestDto getRequestByCode(String collectionCode, User currentUser) {
        FoodRequest request = requestRepository.findByCollectionCode(collectionCode.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Food request not found with collection code: " + collectionCode));

        boolean isReceiver = request.getReceiver().getId().equals(currentUser.getId());
        boolean isDonor = request.getDonation().getDonor().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ROLE_ADMIN;

        if (!isReceiver && !isDonor && !isAdmin) {
            throw new UnauthorizedException("You are not authorized to view this request");
        }

        return new FoodRequestDto(request);
    }

    @Transactional
    public FoodRequestDto updateRequestStatus(Long requestId, RequestActionDto actionDto, User currentUser) {
        FoodRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Food request not found with ID: " + requestId));

        boolean isDonor = request.getDonation().getDonor().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ROLE_ADMIN;
        boolean isReceiver = request.getReceiver().getId().equals(currentUser.getId());

        if (actionDto.getStatus() == RequestStatus.CANCELLED) {
            if (!isReceiver && !isDonor && !isAdmin) {
                throw new UnauthorizedException("Not authorized to cancel this request");
            }
        } else {
            if (!isDonor && !isAdmin) {
                throw new UnauthorizedException("Only the donor or admin can accept, reject, or mark requests ready");
            }
        }

        request.setStatus(actionDto.getStatus());
        if (actionDto.getNotes() != null) {
            request.setNotes(actionDto.getNotes());
        }

        FoodRequest updated = requestRepository.save(request);

        // Recalculate remaining quantity and donation status
        donationService.recalculateDonationStatus(request.getDonation().getId());

        return new FoodRequestDto(updated);
    }

    @Transactional
    public FoodRequestDto verifyAndCompleteCollection(String collectionCode, User currentUser) {
        FoodRequest request = requestRepository.findByCollectionCode(collectionCode.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid collection code: " + collectionCode));

        boolean isDonor = request.getDonation().getDonor().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ROLE_ADMIN;

        if (!isDonor && !isAdmin) {
            throw new UnauthorizedException("Only the listing owner or admin can verify food collection");
        }

        if (request.getStatus() == RequestStatus.COLLECTED) {
            throw new BadRequestException("This request has already been collected");
        }

        request.setStatus(RequestStatus.COLLECTED);
        request.setCollectedAt(LocalDateTime.now());
        FoodRequest saved = requestRepository.save(request);

        donationService.recalculateDonationStatus(request.getDonation().getId());

        return new FoodRequestDto(saved);
    }
}
