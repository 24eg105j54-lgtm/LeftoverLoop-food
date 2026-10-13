package com.leftoverloop.dto;

public class AiMatchRequest {
    private String preferredCategory;
    private String location;
    private Integer requestedQuantity;
    private String receiverType; // INDIVIDUAL, SHELTER, NGO, SCHOOL

    public AiMatchRequest() {}

    public String getPreferredCategory() { return preferredCategory; }
    public void setPreferredCategory(String preferredCategory) { this.preferredCategory = preferredCategory; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Integer getRequestedQuantity() { return requestedQuantity; }
    public void setRequestedQuantity(Integer requestedQuantity) { this.requestedQuantity = requestedQuantity; }

    public String getReceiverType() { return receiverType; }
    public void setReceiverType(String receiverType) { this.receiverType = receiverType; }
}
