package com.aiaudit.platform.team;

import com.aiaudit.platform.TestDataBuilder;
import com.aiaudit.platform.auth.AppUser;
import com.aiaudit.platform.auth.EmailService;
import com.aiaudit.platform.auth.UserRepository;
import com.aiaudit.platform.auth.UserRole;
import com.aiaudit.platform.common.exception.BadRequestException;
import com.aiaudit.platform.common.exception.ResourceNotFoundException;
import com.aiaudit.platform.organization.Organization;
import com.aiaudit.platform.organization.OrganizationRepository;
import com.aiaudit.platform.team.dto.AcceptInvitationRequest;
import com.aiaudit.platform.team.dto.InvitationDto;
import com.aiaudit.platform.team.dto.InviteRequest;
import com.aiaudit.platform.team.dto.TeamMemberDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TeamInvitationRepository invitationRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TeamService teamService;

    // ── getMembers ──

    @Test
    void getMembers_returnsMappedDtoList() {
        UUID orgId = UUID.randomUUID();
        Organization org = TestDataBuilder.anOrganization().id(orgId).build();
        AppUser user1 = TestDataBuilder.anAppUser().organization(org).email("a@test.com").firstName("Alice").build();
        AppUser user2 = TestDataBuilder.anAppUser().organization(org).email("b@test.com").firstName("Bob").build();

        when(userRepository.findByOrganizationIdOrderByCreatedAt(orgId)).thenReturn(List.of(user1, user2));

        List<TeamMemberDto> result = teamService.getMembers(orgId);

        assertEquals(2, result.size());
        assertEquals("Alice", result.get(0).getFirstName());
        assertEquals("Bob", result.get(1).getFirstName());
        verify(userRepository).findByOrganizationIdOrderByCreatedAt(orgId);
    }

    @Test
    void getMembers_emptyOrg_returnsEmptyList() {
        UUID orgId = UUID.randomUUID();
        when(userRepository.findByOrganizationIdOrderByCreatedAt(orgId)).thenReturn(List.of());

        List<TeamMemberDto> result = teamService.getMembers(orgId);

        assertTrue(result.isEmpty());
    }

    // ── getPendingInvitations ──

    @Test
    void getPendingInvitations_filtersExpiredInvitations() {
        UUID orgId = UUID.randomUUID();
        TeamInvitation active = TestDataBuilder.anInvitation()
                .expiresAt(Instant.now().plus(3, ChronoUnit.DAYS))
                .build();
        TeamInvitation expired = TestDataBuilder.anInvitation()
                .expiresAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .build();

        when(invitationRepository.findByOrganizationIdAndAcceptedAtIsNullOrderByCreatedAtDesc(orgId))
                .thenReturn(List.of(active, expired));

        List<InvitationDto> result = teamService.getPendingInvitations(orgId);

        assertEquals(1, result.size());
        assertEquals(active.getEmail(), result.get(0).getEmail());
    }

    @Test
    void getPendingInvitations_noPending_returnsEmptyList() {
        UUID orgId = UUID.randomUUID();
        when(invitationRepository.findByOrganizationIdAndAcceptedAtIsNullOrderByCreatedAtDesc(orgId))
                .thenReturn(List.of());

        List<InvitationDto> result = teamService.getPendingInvitations(orgId);

        assertTrue(result.isEmpty());
    }

    // ── inviteMember ──

    @Test
    void inviteMember_success_createsInvitationAndSendsEmail() {
        UUID orgId = UUID.randomUUID();
        Organization org = TestDataBuilder.anOrganization().id(orgId).build();
        AppUser inviter = TestDataBuilder.anAppUser().organization(org).build();

        InviteRequest request = new InviteRequest();
        request.setEmail("newuser@test.com");
        request.setRole(UserRole.VIEWER);

        when(userRepository.existsByEmail("newuser@test.com")).thenReturn(false);
        when(invitationRepository.existsByEmailAndOrganizationIdAndAcceptedAtIsNull("newuser@test.com", orgId))
                .thenReturn(false);
        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));
        when(invitationRepository.save(any(TeamInvitation.class))).thenAnswer(inv -> inv.getArgument(0));

        InvitationDto result = teamService.inviteMember(orgId, request, inviter);

        assertNotNull(result);
        assertEquals("newuser@test.com", result.getEmail());
        verify(emailService).sendTeamInvitationEmail(eq("newuser@test.com"), any(), eq(org.getName()), eq(inviter.getFirstName()));
        verify(invitationRepository).save(any(TeamInvitation.class));
    }

    @Test
    void inviteMember_emailAlreadyExists_throwsBadRequest() {
        UUID orgId = UUID.randomUUID();
        AppUser inviter = TestDataBuilder.anAppUser().build();

        InviteRequest request = new InviteRequest();
        request.setEmail("existing@test.com");
        request.setRole(UserRole.VIEWER);

        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> teamService.inviteMember(orgId, request, inviter));

        assertEquals("User with this email already exists", ex.getMessage());
        verify(invitationRepository, never()).save(any());
        verify(emailService, never()).sendTeamInvitationEmail(any(), any(), any(), any());
    }

    @Test
    void inviteMember_duplicateInvitation_throwsBadRequest() {
        UUID orgId = UUID.randomUUID();
        AppUser inviter = TestDataBuilder.anAppUser().build();

        InviteRequest request = new InviteRequest();
        request.setEmail("duplicate@test.com");
        request.setRole(UserRole.VIEWER);

        when(userRepository.existsByEmail("duplicate@test.com")).thenReturn(false);
        when(invitationRepository.existsByEmailAndOrganizationIdAndAcceptedAtIsNull("duplicate@test.com", orgId))
                .thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> teamService.inviteMember(orgId, request, inviter));

        assertEquals("An invitation has already been sent to this email", ex.getMessage());
        verify(invitationRepository, never()).save(any());
    }

    // ── removeMember ──

    @Test
    void removeMember_success_disablesUser() {
        UUID orgId = UUID.randomUUID();
        Organization org = TestDataBuilder.anOrganization().id(orgId).build();
        AppUser currentUser = TestDataBuilder.anOwner().organization(org).build();
        AppUser member = TestDataBuilder.anAppUser()
                .id(UUID.randomUUID())
                .organization(org)
                .role(UserRole.VIEWER)
                .build();

        when(userRepository.findById(member.getId())).thenReturn(Optional.of(member));

        teamService.removeMember(member.getId(), orgId, currentUser);

        assertFalse(member.isEnabled());
        verify(userRepository).save(member);
    }

    @Test
    void removeMember_notSameOrg_throwsBadRequest() {
        UUID orgId = UUID.randomUUID();
        Organization otherOrg = TestDataBuilder.anOrganization().id(UUID.randomUUID()).build();
        AppUser currentUser = TestDataBuilder.anOwner().build();
        AppUser member = TestDataBuilder.anAppUser().organization(otherOrg).build();

        when(userRepository.findById(member.getId())).thenReturn(Optional.of(member));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> teamService.removeMember(member.getId(), orgId, currentUser));

        assertEquals("User does not belong to this organization", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void removeMember_self_throwsBadRequest() {
        UUID orgId = UUID.randomUUID();
        Organization org = TestDataBuilder.anOrganization().id(orgId).build();
        AppUser currentUser = TestDataBuilder.anOwner().organization(org).build();

        when(userRepository.findById(currentUser.getId())).thenReturn(Optional.of(currentUser));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> teamService.removeMember(currentUser.getId(), orgId, currentUser));

        assertEquals("Cannot remove yourself", ex.getMessage());
    }

    @Test
    void removeMember_owner_throwsBadRequest() {
        UUID orgId = UUID.randomUUID();
        Organization org = TestDataBuilder.anOrganization().id(orgId).build();
        AppUser currentUser = TestDataBuilder.anAppUser().organization(org).build();
        AppUser owner = TestDataBuilder.anOwner().id(UUID.randomUUID()).organization(org).build();

        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> teamService.removeMember(owner.getId(), orgId, currentUser));

        assertEquals("Cannot remove the organization owner", ex.getMessage());
    }

    // ── acceptInvitation ──

    @Test
    void acceptInvitation_success_createsUserAndMarksAccepted() {
        Organization org = TestDataBuilder.anOrganization().build();
        TeamInvitation invitation = TestDataBuilder.anInvitation()
                .organization(org)
                .email("new@test.com")
                .role(UserRole.ADMIN)
                .expiresAt(Instant.now().plus(3, ChronoUnit.DAYS))
                .acceptedAt(null)
                .build();

        AcceptInvitationRequest request = new AcceptInvitationRequest();
        request.setToken(invitation.getToken());
        request.setPassword("securepassword");
        request.setFirstName("New");
        request.setLastName("User");

        when(invitationRepository.findByToken(invitation.getToken())).thenReturn(Optional.of(invitation));
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("securepassword")).thenReturn("$2a$10$encoded");
        when(userRepository.save(any(AppUser.class))).thenAnswer(inv -> inv.getArgument(0));

        AppUser result = teamService.acceptInvitation(request);

        assertNotNull(result);
        assertEquals("new@test.com", result.getEmail());
        assertEquals("New", result.getFirstName());
        assertEquals("$2a$10$encoded", result.getPasswordHash());
        assertEquals(UserRole.ADMIN, result.getRole());
        assertNotNull(invitation.getAcceptedAt());
        verify(invitationRepository).save(invitation);
    }

    @Test
    void acceptInvitation_invalidToken_throwsBadRequest() {
        AcceptInvitationRequest request = new AcceptInvitationRequest();
        request.setToken("invalid-token");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");

        when(invitationRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> teamService.acceptInvitation(request));

        assertEquals("Invalid invitation token", ex.getMessage());
    }

    @Test
    void acceptInvitation_expired_throwsBadRequest() {
        TeamInvitation invitation = TestDataBuilder.anInvitation()
                .expiresAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .acceptedAt(null)
                .build();

        AcceptInvitationRequest request = new AcceptInvitationRequest();
        request.setToken(invitation.getToken());
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");

        when(invitationRepository.findByToken(invitation.getToken())).thenReturn(Optional.of(invitation));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> teamService.acceptInvitation(request));

        assertEquals("Invitation has expired", ex.getMessage());
    }

    @Test
    void acceptInvitation_alreadyAccepted_throwsBadRequest() {
        TeamInvitation invitation = TestDataBuilder.anInvitation()
                .expiresAt(Instant.now().plus(3, ChronoUnit.DAYS))
                .acceptedAt(Instant.now().minus(1, ChronoUnit.HOURS))
                .build();

        AcceptInvitationRequest request = new AcceptInvitationRequest();
        request.setToken(invitation.getToken());
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");

        when(invitationRepository.findByToken(invitation.getToken())).thenReturn(Optional.of(invitation));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> teamService.acceptInvitation(request));

        assertEquals("Invitation has already been accepted", ex.getMessage());
    }

    @Test
    void acceptInvitation_emailAlreadyTaken_throwsBadRequest() {
        TeamInvitation invitation = TestDataBuilder.anInvitation()
                .expiresAt(Instant.now().plus(3, ChronoUnit.DAYS))
                .acceptedAt(null)
                .build();

        AcceptInvitationRequest request = new AcceptInvitationRequest();
        request.setToken(invitation.getToken());
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");

        when(invitationRepository.findByToken(invitation.getToken())).thenReturn(Optional.of(invitation));
        when(userRepository.existsByEmail(invitation.getEmail())).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> teamService.acceptInvitation(request));

        assertEquals("An account with this email already exists", ex.getMessage());
    }

    // ── cancelInvitation ──

    @Test
    void cancelInvitation_success_deletesInvitation() {
        UUID orgId = UUID.randomUUID();
        Organization org = TestDataBuilder.anOrganization().id(orgId).build();
        TeamInvitation invitation = TestDataBuilder.anInvitation().organization(org).build();

        when(invitationRepository.findById(invitation.getId())).thenReturn(Optional.of(invitation));

        teamService.cancelInvitation(invitation.getId(), orgId);

        verify(invitationRepository).delete(invitation);
    }

    @Test
    void cancelInvitation_wrongOrg_throwsBadRequest() {
        UUID orgId = UUID.randomUUID();
        Organization otherOrg = TestDataBuilder.anOrganization().id(UUID.randomUUID()).build();
        TeamInvitation invitation = TestDataBuilder.anInvitation().organization(otherOrg).build();

        when(invitationRepository.findById(invitation.getId())).thenReturn(Optional.of(invitation));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> teamService.cancelInvitation(invitation.getId(), orgId));

        assertEquals("Invitation does not belong to this organization", ex.getMessage());
        verify(invitationRepository, never()).delete(any());
    }
}
