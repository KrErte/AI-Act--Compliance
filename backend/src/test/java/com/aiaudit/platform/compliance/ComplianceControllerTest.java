package com.aiaudit.platform.compliance;

import com.aiaudit.platform.auth.AppUser;
import com.aiaudit.platform.common.exception.ResourceNotFoundException;
import com.aiaudit.platform.compliance.dto.ObligationDto;
import com.aiaudit.platform.compliance.dto.UpdateObligationRequest;
import com.aiaudit.platform.organization.Organization;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import com.aiaudit.platform.auth.JwtService;
import com.aiaudit.platform.auth.UserRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.aiaudit.platform.TestDataBuilder.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ComplianceController.class)
class ComplianceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ComplianceObligationService obligationService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    private ObjectMapper objectMapper;
    private AppUser mockUser;
    private Organization mockOrg;
    private UUID aiSystemId;
    private UUID obligationId;
    private ObligationDto mockObligationDto;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        mockOrg = anOrganization().build();
        mockUser = anAppUser().organization(mockOrg).build();
        aiSystemId = UUID.randomUUID();
        obligationId = UUID.randomUUID();
        mockObligationDto = ObligationDto.builder()
                .id(obligationId)
                .aiSystemId(aiSystemId)
                .articleRef("Art. 9")
                .articleTitle("Risk Management System")
                .description("Establish and maintain a risk management system")
                .status("NOT_STARTED")
                .dueDate(LocalDate.of(2026, 8, 2))
                .sortOrder(0)
                .build();
    }

    // ────────────────────────────────────────────────────────────────────────
    // GET /ai-systems/{aiSystemId}/obligations
    // ────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /ai-systems/{aiSystemId}/obligations")
    class GetObligations {

        @Test
        @DisplayName("should return 200 with list of obligations")
        void getObligations_returnsList() throws Exception {
            ObligationDto secondObligation = ObligationDto.builder()
                    .id(UUID.randomUUID())
                    .aiSystemId(aiSystemId)
                    .articleRef("Art. 10")
                    .articleTitle("Data and Data Governance")
                    .description("Training, validation and testing data sets shall be relevant")
                    .status("IN_PROGRESS")
                    .dueDate(LocalDate.of(2026, 7, 1))
                    .sortOrder(1)
                    .build();

            when(obligationService.getObligations(aiSystemId, mockOrg.getId()))
                    .thenReturn(List.of(mockObligationDto, secondObligation));

            mockMvc.perform(get("/ai-systems/{aiSystemId}/obligations", aiSystemId)
                            .with(user(mockUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].articleRef").value("Art. 9"))
                    .andExpect(jsonPath("$.data[0].status").value("NOT_STARTED"))
                    .andExpect(jsonPath("$.data[1].articleRef").value("Art. 10"))
                    .andExpect(jsonPath("$.data[1].status").value("IN_PROGRESS"));
        }

        @Test
        @DisplayName("should return 200 with empty list when no obligations exist")
        void getObligations_emptyList() throws Exception {
            when(obligationService.getObligations(aiSystemId, mockOrg.getId()))
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(get("/ai-systems/{aiSystemId}/obligations", aiSystemId)
                            .with(user(mockUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // PATCH /ai-systems/{aiSystemId}/obligations/{obligationId}
    // ────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("PATCH /ai-systems/{aiSystemId}/obligations/{obligationId}")
    class UpdateObligation {

        @Test
        @DisplayName("should return 200 when updating obligation status")
        void updateObligation_status_returns200() throws Exception {
            UpdateObligationRequest request = new UpdateObligationRequest();
            request.setStatus(ObligationStatus.IN_PROGRESS);

            ObligationDto updatedDto = ObligationDto.builder()
                    .id(obligationId)
                    .aiSystemId(aiSystemId)
                    .articleRef("Art. 9")
                    .articleTitle("Risk Management System")
                    .description("Establish and maintain a risk management system")
                    .status("IN_PROGRESS")
                    .dueDate(LocalDate.of(2026, 8, 2))
                    .sortOrder(0)
                    .build();

            when(obligationService.updateObligation(
                    eq(obligationId), eq(aiSystemId), eq(mockOrg.getId()),
                    any(UpdateObligationRequest.class), any(AppUser.class)))
                    .thenReturn(updatedDto);

            mockMvc.perform(patch("/ai-systems/{aiSystemId}/obligations/{obligationId}", aiSystemId, obligationId)
                            .with(user(mockUser)).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(obligationId.toString()))
                    .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));

            verify(obligationService).updateObligation(
                    eq(obligationId), eq(aiSystemId), eq(mockOrg.getId()),
                    any(UpdateObligationRequest.class), any(AppUser.class));
        }

        @Test
        @DisplayName("should return 200 when updating obligation due date")
        void updateObligation_dueDate_returns200() throws Exception {
            UpdateObligationRequest request = new UpdateObligationRequest();
            request.setDueDate(LocalDate.of(2026, 6, 15));

            ObligationDto updatedDto = ObligationDto.builder()
                    .id(obligationId)
                    .aiSystemId(aiSystemId)
                    .articleRef("Art. 9")
                    .articleTitle("Risk Management System")
                    .description("Establish and maintain a risk management system")
                    .status("NOT_STARTED")
                    .dueDate(LocalDate.of(2026, 6, 15))
                    .sortOrder(0)
                    .build();

            when(obligationService.updateObligation(
                    eq(obligationId), eq(aiSystemId), eq(mockOrg.getId()),
                    any(UpdateObligationRequest.class), any(AppUser.class)))
                    .thenReturn(updatedDto);

            mockMvc.perform(patch("/ai-systems/{aiSystemId}/obligations/{obligationId}", aiSystemId, obligationId)
                            .with(user(mockUser)).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.dueDate").value("2026-06-15"));
        }

        @Test
        @DisplayName("should return 200 when updating obligation notes")
        void updateObligation_notes_returns200() throws Exception {
            UpdateObligationRequest request = new UpdateObligationRequest();
            request.setNotes("Added initial risk assessment notes");

            ObligationDto updatedDto = ObligationDto.builder()
                    .id(obligationId)
                    .aiSystemId(aiSystemId)
                    .articleRef("Art. 9")
                    .articleTitle("Risk Management System")
                    .description("Establish and maintain a risk management system")
                    .status("NOT_STARTED")
                    .notes("Added initial risk assessment notes")
                    .dueDate(LocalDate.of(2026, 8, 2))
                    .sortOrder(0)
                    .build();

            when(obligationService.updateObligation(
                    eq(obligationId), eq(aiSystemId), eq(mockOrg.getId()),
                    any(UpdateObligationRequest.class), any(AppUser.class)))
                    .thenReturn(updatedDto);

            mockMvc.perform(patch("/ai-systems/{aiSystemId}/obligations/{obligationId}", aiSystemId, obligationId)
                            .with(user(mockUser)).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.notes").value("Added initial risk assessment notes"));
        }

        @Test
        @DisplayName("should return 200 when updating assignedTo")
        void updateObligation_assignedTo_returns200() throws Exception {
            UUID assigneeId = UUID.randomUUID();
            UpdateObligationRequest request = new UpdateObligationRequest();
            request.setAssignedTo(assigneeId);

            ObligationDto updatedDto = ObligationDto.builder()
                    .id(obligationId)
                    .aiSystemId(aiSystemId)
                    .articleRef("Art. 9")
                    .articleTitle("Risk Management System")
                    .description("Establish and maintain a risk management system")
                    .status("NOT_STARTED")
                    .assignedTo(assigneeId)
                    .dueDate(LocalDate.of(2026, 8, 2))
                    .sortOrder(0)
                    .build();

            when(obligationService.updateObligation(
                    eq(obligationId), eq(aiSystemId), eq(mockOrg.getId()),
                    any(UpdateObligationRequest.class), any(AppUser.class)))
                    .thenReturn(updatedDto);

            mockMvc.perform(patch("/ai-systems/{aiSystemId}/obligations/{obligationId}", aiSystemId, obligationId)
                            .with(user(mockUser)).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.assignedTo").value(assigneeId.toString()));
        }

        @Test
        @DisplayName("should return 404 when obligation is not found")
        void updateObligation_obligationNotFound_returns404() throws Exception {
            UUID unknownObligationId = UUID.randomUUID();
            UpdateObligationRequest request = new UpdateObligationRequest();
            request.setStatus(ObligationStatus.COMPLETED);

            when(obligationService.updateObligation(
                    eq(unknownObligationId), eq(aiSystemId), eq(mockOrg.getId()),
                    any(UpdateObligationRequest.class), any(AppUser.class)))
                    .thenThrow(new ResourceNotFoundException("Obligation", "id", unknownObligationId));

            mockMvc.perform(patch("/ai-systems/{aiSystemId}/obligations/{obligationId}", aiSystemId, unknownObligationId)
                            .with(user(mockUser)).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Obligation not found with id: '" + unknownObligationId + "'"));
        }

        @Test
        @DisplayName("should return 404 when AI system is not found")
        void updateObligation_systemNotFound_returns404() throws Exception {
            UUID unknownSystemId = UUID.randomUUID();
            UpdateObligationRequest request = new UpdateObligationRequest();
            request.setStatus(ObligationStatus.COMPLETED);

            when(obligationService.updateObligation(
                    eq(obligationId), eq(unknownSystemId), eq(mockOrg.getId()),
                    any(UpdateObligationRequest.class), any(AppUser.class)))
                    .thenThrow(new ResourceNotFoundException("AI System", "id", unknownSystemId));

            mockMvc.perform(patch("/ai-systems/{aiSystemId}/obligations/{obligationId}", unknownSystemId, obligationId)
                            .with(user(mockUser)).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("AI System not found with id: '" + unknownSystemId + "'"));
        }
    }
}
