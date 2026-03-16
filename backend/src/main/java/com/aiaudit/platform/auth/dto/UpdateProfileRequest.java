package com.aiaudit.platform.auth.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String firstName;
    private String lastName;
    private String languagePreference;
}
