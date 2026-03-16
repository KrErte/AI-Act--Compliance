package com.aiaudit.platform.team.dto;

import com.aiaudit.platform.auth.AppUser;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class TeamMemberDto {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private Instant joinedAt;

    public static TeamMemberDto from(AppUser user) {
        return TeamMemberDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .joinedAt(user.getCreatedAt())
                .build();
    }
}
