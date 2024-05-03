package com.gytni.licenseclassify.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Evidence {
    private String title;
    private String url;
    private String excerpt;
    private String summary;

    @JsonProperty("official_site")
    private boolean officialSite;
    private EvidenceLicense license;
    private String body;
    private int score;

}
