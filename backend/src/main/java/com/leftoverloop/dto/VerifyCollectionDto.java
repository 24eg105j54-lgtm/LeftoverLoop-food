package com.leftoverloop.dto;

import jakarta.validation.constraints.NotBlank;

public class VerifyCollectionDto {

    @NotBlank(message = "Collection code is required")
    private String collectionCode;

    public VerifyCollectionDto() {}

    public VerifyCollectionDto(String collectionCode) {
        this.collectionCode = collectionCode;
    }

    public String getCollectionCode() { return collectionCode; }
    public void setCollectionCode(String collectionCode) { this.collectionCode = collectionCode; }
}
