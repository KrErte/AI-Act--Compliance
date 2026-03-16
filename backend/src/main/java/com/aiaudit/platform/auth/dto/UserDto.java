package com.aiaudit.platform.auth.dto;

import com.aiaudit.platform.auth.AppUser;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserDto {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private UUID organizationId;
    private String organizationName;

    public static UserDto from(AppUser user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .organizationId(user.getOrganization().getId())
                .organizationName(user.getOrganization().getName())
                .build();
    }
}
