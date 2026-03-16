package com.aiaudit.platform.team;

import com.aiaudit.platform.auth.AppUser;
import com.aiaudit.platform.auth.UserRepository;
import com.aiaudit.platform.auth.UserRole;
import com.aiaudit.platform.auth.EmailService;
import com.aiaudit.platform.common.exception.BadRequestException;
import com.aiaudit.platform.common.exception.ResourceNotFoundException;
import com.aiaudit.platform.organization.Organization;
import com.aiaudit.platform.organization.OrganizationRepository;
import com.aiaudit.platform.team.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamService {

    private final UserRepository userRepository;
    private final TeamInvitationRepository invitationRepository;
    private final OrganizationRepository organizationRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<TeamMemberDto> getMembers(UUID organizationId) {
        return userRepository.findByOrganizationIdOrderByCreatedAt(organizationId).stream()
                .map(TeamMemberDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InvitationDto> getPendingInvitations(UUID organizationId) {
        return invitationRepository.findByOrganizationIdAndAcceptedAtIsNullOrderByCreatedAtDesc(organizationId).stream()
                .filter(inv -> !inv.isExpired())
                .map(InvitationDto::from)
                .toList();
    }

    @Transactional
    public InvitationDto inviteMember(UUID organizationId, InviteRequest request, AppUser inviter) {
        String email = request.getEmail().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("User with this email already exists");
        }
        if (invitationRepository.existsByEmailAndOrganizationIdAndAcceptedAtIsNull(email, organizationId)) {
            throw new BadRequestException("An invitation has already been sent to this email");
        }

        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", organizationId));

        String token = UUID.randomUUID().toString();
        TeamInvitation invitation = TeamInvitation.builder()
                .email(email)
                .role(request.getRole())
                .token(token)
                .organization(org)
                .invitedBy(inviter)
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .build();

        invitation = invitationRepository.save(invitation);
        emailService.sendTeamInvitationEmail(email, token, org.getName(), inviter.getFirstName());
        return InvitationDto.from(invitation);
    }

    @Transactional
    public void removeMember(UUID memberId, UUID organizationId, AppUser currentUser) {
        AppUser member = userRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", memberId));

        if (!member.getOrganization().getId().equals(organizationId)) {
            throw new BadRequestException("User does not belong to this organization");
        }
        if (member.getId().equals(currentUser.getId())) {
            throw new BadRequestException("Cannot remove yourself");
        }
        if (member.getRole() == UserRole.OWNER) {
            throw new BadRequestException("Cannot remove the organization owner");
        }

        member.setEnabled(false);
        userRepository.save(member);
    }

    @Transactional
    public AppUser acceptInvitation(AcceptInvitationRequest request) {
        TeamInvitation invitation = invitationRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid invitation token"));

        if (invitation.isExpired()) {
            throw new BadRequestException("Invitation has expired");
        }
        if (invitation.isAccepted()) {
            throw new BadRequestException("Invitation has already been accepted");
        }
        if (userRepository.existsByEmail(invitation.getEmail())) {
            throw new BadRequestException("An account with this email already exists");
        }

        AppUser user = AppUser.builder()
                .email(invitation.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(invitation.getRole())
                .organization(invitation.getOrganization())
                .build();
        user = userRepository.save(user);

        invitation.setAcceptedAt(Instant.now());
        invitationRepository.save(invitation);

        return user;
    }

    @Transactional
    public void cancelInvitation(UUID invitationId, UUID organizationId) {
        TeamInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation", "id", invitationId));
        if (!invitation.getOrganization().getId().equals(organizationId)) {
            throw new BadRequestException("Invitation does not belong to this organization");
        }
        invitationRepository.delete(invitation);
    }
}
