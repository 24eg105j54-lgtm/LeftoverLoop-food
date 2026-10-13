package com.leftoverloop.dto;

import com.leftoverloop.entity.RequestStatus;
import jakarta.validation.constraints.NotNull;

public class RequestActionDto {

    @NotNull(message = "Action status is required")
    private RequestStatus status;

    private String notes;

    public RequestActionDto() {}

    public RequestActionDto(RequestStatus status, String notes) {
        this.status = status;
        this.notes = notes;
    }

    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
