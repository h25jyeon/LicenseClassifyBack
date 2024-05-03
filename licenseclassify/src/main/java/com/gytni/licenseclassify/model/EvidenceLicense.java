package com.gytni.licenseclassify.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class EvidenceLicense {
    @JsonProperty("license_type")
    private String licenseType;
    private float certainty;
}
