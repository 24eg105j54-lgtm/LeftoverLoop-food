package com.leftoverloop.service;

import com.leftoverloop.dto.FoodDonationDto;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FoodDonationService {

    @Autowired
    private FoodDonationRepository donationRepository;

    @Autowired
    private FoodRequestRepository requestRepository;

    @Transactional
    public FoodDonationDto createDonation(FoodDonationDto dto, User currentDonor) {
        if (currentDonor.getRole() != Role.ROLE_DONOR && currentDonor.getRole() != Role.ROLE_ORGANIZATION && currentDonor.getRole() != Role.ROLE_ADMIN) {
            throw new UnauthorizedException("Only Donors, Organizations, and Admins can post food donations");
        }

        if (dto.getExpiryDate() == null || dto.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Expiry date must be in the future");
        }

        if (dto.getInitialQuantity() == null || dto.getInitialQuantity() <= 0) {
            throw new BadRequestException("Initial quantity must be greater than zero");
        }

        FoodDonation donation = new FoodDonation();
        donation.setTitle(dto.getTitle());
        donation.setCategory(dto.getCategory());
        donation.setDescription(dto.getDescription());
        donation.setInitialQuantity(dto.getInitialQuantity());
        donation.setUnit(dto.getUnit());
        donation.setPreparationTime(dto.getPreparationTime() != null ? dto.getPreparationTime() : LocalDateTime.now());
        donation.setExpiryDate(dto.getExpiryDate());
        donation.setLocation(dto.getLocation());
        donation.setPickupInstructions(dto.getPickupInstructions());
        donation.setImageUrl(dto.getImageUrl());
        donation.setDonor(currentDonor);
        donation.setStatus(DonationStatus.AVAILABLE);

        FoodDonation saved = donationRepository.save(donation);
        return convertToDto(saved);
    }

    public List<FoodDonationDto> getAllActiveDonations(String category, String location) {
        updateExpiredDonationsStatus();
        LocalDateTime now = LocalDateTime.now();
        List<DonationStatus> activeStatuses = Arrays.asList(DonationStatus.AVAILABLE, DonationStatus.PARTIALLY_RESERVED);

        List<FoodDonation> donations = donationRepository.findActiveDonations(activeStatuses, now);

        return donations.stream()
                .map(this::convertToDto)
                .filter(d -> d.getRemainingQuantity() > 0)
                .filter(d -> category == null || category.isEmpty() || d.getCategory().equalsIgnoreCase(category))
                .filter(d -> location == null || location.isEmpty() || d.getLocation().toLowerCase().contains(location.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<FoodDonationDto> getDonationsByDonor(Long donorId) {
        updateExpiredDonationsStatus();
        List<FoodDonation> donations = donationRepository.findByDonorIdOrderByCreatedAtDesc(donorId);
        return donations.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public List<FoodDonationDto> getOrganizationDonations() {
        updateExpiredDonationsStatus();
        List<FoodDonation> donations = donationRepository.findByDonorRoleOrderByCreatedAtDesc(Role.ROLE_ORGANIZATION);
        return donations.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public FoodDonationDto getDonationById(Long id) {
        FoodDonation donation = donationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food donation not found with ID: " + id));
        checkAndUpdateExpiry(donation);
        return convertToDto(donation);
    }

    @Transactional
    public FoodDonationDto updateDonation(Long id, FoodDonationDto dto, User currentUser) {
        FoodDonation donation = donationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food donation not found with ID: " + id));

        if (!donation.getDonor().getId().equals(currentUser.getId()) && currentUser.getRole() != Role.ROLE_ADMIN) {
            throw new UnauthorizedException("You do not have permission to modify this donation");
        }

        if (dto.getExpiryDate() != null && dto.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Expiry date must be in the future");
        }

        if (dto.getTitle() != null) donation.setTitle(dto.getTitle());
        if (dto.getCategory() != null) donation.setCategory(dto.getCategory());
        if (dto.getDescription() != null) donation.setDescription(dto.getDescription());
        if (dto.getInitialQuantity() != null) donation.setInitialQuantity(dto.getInitialQuantity());
        if (dto.getUnit() != null) donation.setUnit(dto.getUnit());
        if (dto.getPreparationTime() != null) donation.setPreparationTime(dto.getPreparationTime());
        if (dto.getExpiryDate() != null) donation.setExpiryDate(dto.getExpiryDate());
        if (dto.getLocation() != null) donation.setLocation(dto.getLocation());
        if (dto.getPickupInstructions() != null) donation.setPickupInstructions(dto.getPickupInstructions());
        if (dto.getImageUrl() != null) donation.setImageUrl(dto.getImageUrl());

        FoodDonation updated = donationRepository.save(donation);
        recalculateDonationStatus(updated.getId());
        return convertToDto(updated);
    }

    @Transactional
    public void deleteDonation(Long id, User currentUser) {
        FoodDonation donation = donationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food donation not found with ID: " + id));

        if (!donation.getDonor().getId().equals(currentUser.getId()) && currentUser.getRole() != Role.ROLE_ADMIN) {
            throw new UnauthorizedException("You do not have permission to delete this donation");
        }

        donation.setStatus(DonationStatus.CANCELLED);
        donationRepository.save(donation);
    }

    public Integer calculateRemainingQuantity(Long donationId, Integer initialQuantity) {
        List<RequestStatus> reservedStatuses = Arrays.asList(
                RequestStatus.PENDING,
                RequestStatus.ACCEPTED,
                RequestStatus.READY_FOR_COLLECTION,
                RequestStatus.COLLECTED
        );
        Integer reservedUnits = requestRepository.sumReservedQuantityForDonation(donationId, reservedStatuses);
        int remaining = initialQuantity - (reservedUnits != null ? reservedUnits : 0);
        return Math.max(0, remaining);
    }

    @Transactional
    public void recalculateDonationStatus(Long donationId) {
        FoodDonation donation = donationRepository.findById(donationId).orElse(null);
        if (donation == null) return;

        if (donation.getExpiryDate() != null && donation.getExpiryDate().isBefore(LocalDateTime.now())) {
            donation.setStatus(DonationStatus.EXPIRED);
            donationRepository.save(donation);
            return;
        }

        Integer remaining = calculateRemainingQuantity(donation.getId(), donation.getInitialQuantity());

        if (remaining == 0) {
            donation.setStatus(DonationStatus.RESERVED);
        } else if (remaining < donation.getInitialQuantity()) {
            donation.setStatus(DonationStatus.PARTIALLY_RESERVED);
        } else {
            donation.setStatus(DonationStatus.AVAILABLE);
        }

        donationRepository.save(donation);
    }

    @Transactional
    public void updateExpiredDonationsStatus() {
        LocalDateTime now = LocalDateTime.now();
        List<FoodDonation> expiredList = donationRepository.findNewlyExpiredDonations(now);
        for (FoodDonation donation : expiredList) {
            donation.setStatus(DonationStatus.EXPIRED);
            donationRepository.save(donation);
        }
    }

    private void checkAndUpdateExpiry(FoodDonation donation) {
        if (donation.getExpiryDate() != null && donation.getExpiryDate().isBefore(LocalDateTime.now()) && donation.getStatus() != DonationStatus.EXPIRED) {
            donation.setStatus(DonationStatus.EXPIRED);
            donationRepository.save(donation);
        }
    }

    public FoodDonationDto convertToDto(FoodDonation donation) {
        Integer remaining = calculateRemainingQuantity(donation.getId(), donation.getInitialQuantity());
        return new FoodDonationDto(donation, remaining);
    }
}
