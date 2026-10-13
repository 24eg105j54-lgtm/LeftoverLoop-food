package com.leftoverloop.dto;

import com.leftoverloop.entity.FoodRequest;
import com.leftoverloop.entity.RequestStatus;

import java.time.LocalDateTime;

public class FoodRequestDto {
    private Long id;
    private Long donationId;
    private String donationTitle;
    private String donationCategory;
    private String donationUnit;
    private String donationLocation;
    private String pickupInstructions;
    private LocalDateTime expiryDate;
    private Long donorId;
    private String donorName;
    private String donorOrganization;
    private String donorPhone;
    private Long receiverId;
    private String receiverName;
    private String receiverEmail;
    private String receiverPhone;
    private String receiverOrganization;
    private Integer requestedQuantity;
    private RequestStatus status;
    private String statusColor; // RED, ORANGE, GREEN
    private String collectionCode;
    private String notes;
    private LocalDateTime requestedAt;
    private LocalDateTime updatedAt;
    private LocalDateTime collectedAt;

    public FoodRequestDto() {}

    public FoodRequestDto(FoodRequest request) {
        this.id = request.getId();
        this.requestedQuantity = request.getRequestedQuantity();
        this.status = request.getStatus();
        this.collectionCode = request.getCollectionCode();
        this.notes = request.getNotes();
        this.requestedAt = request.getRequestedAt();
        this.updatedAt = request.getUpdatedAt();
        this.collectedAt = request.getCollectedAt();

        if (request.getDonation() != null) {
            this.donationId = request.getDonation().getId();
            this.donationTitle = request.getDonation().getTitle();
            this.donationCategory = request.getDonation().getCategory();
            this.donationUnit = request.getDonation().getUnit();
            this.donationLocation = request.getDonation().getLocation();
            this.pickupInstructions = request.getDonation().getPickupInstructions();
            this.expiryDate = request.getDonation().getExpiryDate();

            if (request.getDonation().getDonor() != null) {
                this.donorId = request.getDonation().getDonor().getId();
                this.donorName = request.getDonation().getDonor().getFullName();
                this.donorOrganization = request.getDonation().getDonor().getOrganizationName();
                this.donorPhone = request.getDonation().getDonor().getPhone();
            }
        }

        if (request.getReceiver() != null) {
            this.receiverId = request.getReceiver().getId();
            this.receiverName = request.getReceiver().getFullName();
            this.receiverEmail = request.getReceiver().getEmail();
            this.receiverPhone = request.getReceiver().getPhone();
            this.receiverOrganization = request.getReceiver().getOrganizationName();
        }

        // Color coding for requests
        if (request.getStatus() == RequestStatus.PENDING) {
            this.statusColor = "ORANGE";
        } else if (request.getStatus() == RequestStatus.ACCEPTED || request.getStatus() == RequestStatus.READY_FOR_COLLECTION) {
            this.statusColor = "BLUE";
        } else if (request.getStatus() == RequestStatus.COLLECTED) {
            this.statusColor = "PURPLE";
        } else {
            this.statusColor = "RED"; // REJECTED or CANCELLED
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDonationId() { return donationId; }
    public void setDonationId(Long donationId) { this.donationId = donationId; }

    public String getDonationTitle() { return donationTitle; }
    public void setDonationTitle(String donationTitle) { this.donationTitle = donationTitle; }

    public String getDonationCategory() { return donationCategory; }
    public void setDonationCategory(String donationCategory) { this.donationCategory = donationCategory; }

    public String getDonationUnit() { return donationUnit; }
    public void setDonationUnit(String donationUnit) { this.donationUnit = donationUnit; }

    public String getDonationLocation() { return donationLocation; }
    public void setDonationLocation(String donationLocation) { this.donationLocation = donationLocation; }

    public String getPickupInstructions() { return pickupInstructions; }
    public void setPickupInstructions(String pickupInstructions) { this.pickupInstructions = pickupInstructions; }

    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }

    public Long getDonorId() { return donorId; }
    public void setDonorId(Long donorId) { this.donorId = donorId; }

    public String getDonorName() { return donorName; }
    public void setDonorName(String donorName) { this.donorName = donorName; }

    public String getDonorOrganization() { return donorOrganization; }
    public void setDonorOrganization(String donorOrganization) { this.donorOrganization = donorOrganization; }

    public String getDonorPhone() { return donorPhone; }
    public void setDonorPhone(String donorPhone) { this.donorPhone = donorPhone; }

    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }

    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }

    public String getReceiverEmail() { return receiverEmail; }
    public void setReceiverEmail(String receiverEmail) { this.receiverEmail = receiverEmail; }

    public String getReceiverPhone() { return receiverPhone; }
    public void setReceiverPhone(String receiverPhone) { this.receiverPhone = receiverPhone; }

    public String getReceiverOrganization() { return receiverOrganization; }
    public void setReceiverOrganization(String receiverOrganization) { this.receiverOrganization = receiverOrganization; }

    public Integer getRequestedQuantity() { return requestedQuantity; }
    public void setRequestedQuantity(Integer requestedQuantity) { this.requestedQuantity = requestedQuantity; }

    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }

    public String getStatusColor() { return statusColor; }
    public void setStatusColor(String statusColor) { this.statusColor = statusColor; }

    public String getCollectionCode() { return collectionCode; }
    public void setCollectionCode(String collectionCode) { this.collectionCode = collectionCode; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getCollectedAt() { return collectedAt; }
    public void setCollectedAt(LocalDateTime collectedAt) { this.collectedAt = collectedAt; }
}
