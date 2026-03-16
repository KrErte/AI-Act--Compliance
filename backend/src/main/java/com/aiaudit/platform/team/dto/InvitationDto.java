package com.aiaudit.platform.team.dto;

import com.aiaudit.platform.team.TeamInvitation;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class InvitationDto {
    private UUID id;
    private String email;
    private String role;
    private Instant expiresAt;
    private Instant createdAt;

    public static InvitationDto from(TeamInvitation invitation) {
        return InvitationDto.builder()
                .id(invitation.getId())
                .email(invitation.getEmail())
                .role(invitation.getRole().name())
                .expiresAt(invitation.getExpiresAt())
                .createdAt(invitation.getCreatedAt())
                .build();
    }
}
