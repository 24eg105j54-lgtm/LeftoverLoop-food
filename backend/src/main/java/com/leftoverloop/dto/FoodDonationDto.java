package com.leftoverloop.dto;

import com.leftoverloop.entity.DonationStatus;
import com.leftoverloop.entity.FoodDonation;

import java.time.LocalDateTime;

public class FoodDonationDto {
    private Long id;
    private String title;
    private String category;
    private String description;
    private Integer initialQuantity;
    private Integer remainingQuantity;
    private String unit;
    private LocalDateTime preparationTime;
    private LocalDateTime expiryDate;
    private String location;
    private String pickupInstructions;
    private String imageUrl;
    private Long donorId;
    private String donorName;
    private String donorRole;
    private String donorOrganization;
    private String donorPhone;
    private DonationStatus status;
    private String statusColor; // RED, ORANGE, GREEN
    private LocalDateTime createdAt;
    private boolean expired;

    public FoodDonationDto() {}

    public FoodDonationDto(FoodDonation donation, Integer remainingQuantity) {
        this.id = donation.getId();
        this.title = donation.getTitle();
        this.category = donation.getCategory();
        this.description = donation.getDescription();
        this.initialQuantity = donation.getInitialQuantity();
        this.remainingQuantity = remainingQuantity != null ? remainingQuantity : donation.getInitialQuantity();
        this.unit = donation.getUnit();
        this.preparationTime = donation.getPreparationTime();
        this.expiryDate = donation.getExpiryDate();
        this.location = donation.getLocation();
        this.pickupInstructions = donation.getPickupInstructions();
        this.imageUrl = donation.getImageUrl();
        this.status = donation.getStatus();
        this.createdAt = donation.getCreatedAt();

        if (donation.getDonor() != null) {
            this.donorId = donation.getDonor().getId();
            this.donorName = donation.getDonor().getFullName();
            this.donorRole = donation.getDonor().getRole().name();
            this.donorOrganization = donation.getDonor().getOrganizationName();
            this.donorPhone = donation.getDonor().getPhone();
        }

        LocalDateTime now = LocalDateTime.now();
        this.expired = donation.getExpiryDate() != null && donation.getExpiryDate().isBefore(now);

        // Determine statusColor (RED, ORANGE, GREEN)
        if (this.expired || donation.getStatus() == DonationStatus.EXPIRED) {
            this.statusColor = "RED";
        } else if (donation.getExpiryDate() != null && donation.getExpiryDate().isBefore(now.plusHours(4))) {
            this.statusColor = "RED"; // Highly urgent (< 4 hrs)
        } else if (donation.getExpiryDate() != null && donation.getExpiryDate().isBefore(now.plusHours(12))) {
            this.statusColor = "ORANGE"; // Approaching expiry (< 12 hrs) or partial reserve
        } else if (donation.getStatus() == DonationStatus.PARTIALLY_RESERVED) {
            this.statusColor = "ORANGE";
        } else {
            this.statusColor = "GREEN";
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getInitialQuantity() { return initialQuantity; }
    public void setInitialQuantity(Integer initialQuantity) { this.initialQuantity = initialQuantity; }

    public Integer getRemainingQuantity() { return remainingQuantity; }
    public void setRemainingQuantity(Integer remainingQuantity) { this.remainingQuantity = remainingQuantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public LocalDateTime getPreparationTime() { return preparationTime; }
    public void setPreparationTime(LocalDateTime preparationTime) { this.preparationTime = preparationTime; }

    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getPickupInstructions() { return pickupInstructions; }
    public void setPickupInstructions(String pickupInstructions) { this.pickupInstructions = pickupInstructions; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Long getDonorId() { return donorId; }
    public void setDonorId(Long donorId) { this.donorId = donorId; }

    public String getDonorName() { return donorName; }
    public void setDonorName(String donorName) { this.donorName = donorName; }

    public String getDonorRole() { return donorRole; }
    public void setDonorRole(String donorRole) { this.donorRole = donorRole; }

    public String getDonorOrganization() { return donorOrganization; }
    public void setDonorOrganization(String donorOrganization) { this.donorOrganization = donorOrganization; }

    public String getDonorPhone() { return donorPhone; }
    public void setDonorPhone(String donorPhone) { this.donorPhone = donorPhone; }

    public DonationStatus getStatus() { return status; }
    public void setStatus(DonationStatus status) { this.status = status; }

    public String getStatusColor() { return statusColor; }
    public void setStatusColor(String statusColor) { this.statusColor = statusColor; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isExpired() { return expired; }
    public void setExpired(boolean expired) { this.expired = expired; }
}
