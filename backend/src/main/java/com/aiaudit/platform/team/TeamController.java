package com.aiaudit.platform.team;

import com.aiaudit.platform.auth.AppUser;
import com.aiaudit.platform.auth.dto.AuthResponse;
import com.aiaudit.platform.auth.JwtService;
import com.aiaudit.platform.auth.RefreshTokenService;
import com.aiaudit.platform.auth.RefreshToken;
import com.aiaudit.platform.auth.dto.UserDto;
import com.aiaudit.platform.common.ApiResponse;
import com.aiaudit.platform.team.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @GetMapping("/team/members")
    public ResponseEntity<ApiResponse<List<TeamMemberDto>>> getMembers(@AuthenticationPrincipal AppUser user) {
        List<TeamMemberDto> members = teamService.getMembers(user.getOrganization().getId());
        return ResponseEntity.ok(ApiResponse.success(members));
    }

    @GetMapping("/team/invitations")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<InvitationDto>>> getPendingInvitations(@AuthenticationPrincipal AppUser user) {
        List<InvitationDto> invitations = teamService.getPendingInvitations(user.getOrganization().getId());
        return ResponseEntity.ok(ApiResponse.success(invitations));
    }

    @PostMapping("/team/invitations")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<InvitationDto>> inviteMember(
            @AuthenticationPrincipal AppUser user,
            @Valid @RequestBody InviteRequest request) {
        InvitationDto dto = teamService.inviteMember(user.getOrganization().getId(), request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(dto, "Invitation sent"));
    }

    @DeleteMapping("/team/members/{memberId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @AuthenticationPrincipal AppUser user,
            @PathVariable UUID memberId) {
        teamService.removeMember(memberId, user.getOrganization().getId(), user);
        return ResponseEntity.ok(ApiResponse.success(null, "Member removed"));
    }

    @DeleteMapping("/team/invitations/{invitationId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> cancelInvitation(
            @AuthenticationPrincipal AppUser user,
            @PathVariable UUID invitationId) {
        teamService.cancelInvitation(invitationId, user.getOrganization().getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Invitation cancelled"));
    }

    @PostMapping("/auth/accept-invitation")
    public ResponseEntity<ApiResponse<AuthResponse>> acceptInvitation(
            @Valid @RequestBody AcceptInvitationRequest request) {
        AppUser user = teamService.acceptInvitation(request);
        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        AuthResponse response = AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .user(UserDto.from(user))
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }
}
