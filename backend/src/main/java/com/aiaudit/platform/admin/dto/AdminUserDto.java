package com.aiaudit.platform.admin.dto;

import com.aiaudit.platform.auth.AppUser;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class AdminUserDto {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;

    public static AdminUserDto from(AppUser user) {
        return AdminUserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
