package com.aiaudit.platform.aisystem;

import com.aiaudit.platform.aisystem.dto.AiSystemDto;
import com.aiaudit.platform.aisystem.dto.CreateAiSystemRequest;
import com.aiaudit.platform.aisystem.dto.UpdateAiSystemRequest;
import com.aiaudit.platform.auth.AppUser;
import com.aiaudit.platform.common.PagedResponse;
import com.aiaudit.platform.common.exception.ResourceNotFoundException;
import com.aiaudit.platform.common.exception.SubscriptionLimitException;
import com.aiaudit.platform.organization.Organization;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.aiaudit.platform.TestDataBuilder.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AiSystemController.class)
class AiSystemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AiSystemService aiSystemService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    private ObjectMapper objectMapper;
    private AppUser mockUser;
    private Organization mockOrg;
    private AiSystemDto mockDto;
    private UUID systemId;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        mockOrg = anOrganization().build();
        mockUser = anAppUser().organization(mockOrg).build();
        systemId = UUID.randomUUID();
        mockDto = AiSystemDto.builder()
                .id(systemId)
                .name("Test AI System")
                .description("A test system")
                .vendor("Test Vendor")
                .version("1.0")
                .purpose("Testing")
                .deploymentContext("INTERNAL")
                .organizationRole("DEPLOYER")
                .status("ACTIVE")
                .riskLevel("HIGH")
                .complianceScore(0)
                .complianceStatus("NOT_STARTED")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    // ────────────────────────────────────────────────────────────────────────
    // GET /ai-systems
    // ────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /ai-systems")
    class ListAiSystems {

        @Test
        @DisplayName("should return 200 with paged list of AI systems")
        void listAiSystems_returnsPaged() throws Exception {
            PagedResponse<AiSystemDto> pagedResponse = PagedResponse.<AiSystemDto>builder()
                    .content(List.of(mockDto))
                    .page(0)
                    .size(20)
                    .totalElements(1)
                    .totalPages(1)
                    .last(true)
                    .build();

            when(aiSystemService.listAiSystems(
                    eq(mockOrg.getId()), isNull(), isNull(), isNull(),
                    isNull(), isNull(), eq(0), eq(20)))
                    .thenReturn(pagedResponse);

            mockMvc.perform(get("/ai-systems")
                            .with(user(mockUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content[0].name").value("Test AI System"))
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.totalPages").value(1))
                    .andExpect(jsonPath("$.data.last").value(true));
        }

        @Test
        @DisplayName("should pass filter parameters to the service")
        void listAiSystems_withFilters() throws Exception {
            PagedResponse<AiSystemDto> pagedResponse = PagedResponse.<AiSystemDto>builder()
                    .content(List.of(mockDto))
                    .page(0)
                    .size(20)
                    .totalElements(1)
                    .totalPages(1)
                    .last(true)
                    .build();

            when(aiSystemService.listAiSystems(
                    eq(mockOrg.getId()), isNull(), eq(RiskLevel.HIGH), isNull(),
                    isNull(), isNull(), eq(0), eq(20)))
                    .thenReturn(pagedResponse);

            mockMvc.perform(get("/ai-systems")
                            .param("riskLevel", "HIGH")
                            .with(user(mockUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray());

            verify(aiSystemService).listAiSystems(
                    eq(mockOrg.getId()), isNull(), eq(RiskLevel.HIGH), isNull(),
                    isNull(), isNull(), eq(0), eq(20));
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // GET /ai-systems/{id}
    // ────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /ai-systems/{id}")
    class GetAiSystem {

        @Test
        @DisplayName("should return 200 with AI system details")
        void getAiSystem_found_returns200() throws Exception {
            when(aiSystemService.getAiSystem(systemId, mockOrg.getId())).thenReturn(mockDto);

            mockMvc.perform(get("/ai-systems/{id}", systemId)
                            .with(user(mockUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(systemId.toString()))
                    .andExpect(jsonPath("$.data.name").value("Test AI System"))
                    .andExpect(jsonPath("$.data.riskLevel").value("HIGH"));
        }

        @Test
        @DisplayName("should return 404 when AI system not found")
        void getAiSystem_notFound_returns404() throws Exception {
            UUID unknownId = UUID.randomUUID();

            when(aiSystemService.getAiSystem(unknownId, mockOrg.getId()))
                    .thenThrow(new ResourceNotFoundException("AI System", "id", unknownId));

            mockMvc.perform(get("/ai-systems/{id}", unknownId)
                            .with(user(mockUser)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("AI System not found with id: '" + unknownId + "'"));
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // POST /ai-systems
    // ────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /ai-systems")
    class CreateAiSystem {

        @Test
        @DisplayName("should return 201 when AI system is created")
        void createAiSystem_valid_returns201() throws Exception {
            CreateAiSystemRequest request = new CreateAiSystemRequest();
            request.setName("New AI System");
            request.setDescription("A new system");
            request.setVendor("Vendor");
            request.setVersion("2.0");
            request.setPurpose("Production");
            request.setDeploymentContext(DeploymentContext.INTERNAL);
            request.setOrganizationRole(OrganizationRole.DEPLOYER);

            when(aiSystemService.createAiSystem(eq(mockOrg.getId()), any(CreateAiSystemRequest.class), any(AppUser.class)))
                    .thenReturn(mockDto);

            mockMvc.perform(post("/ai-systems")
                            .with(user(mockUser)).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("AI system created"))
                    .andExpect(jsonPath("$.data.name").value("Test AI System"));

            verify(aiSystemService).createAiSystem(eq(mockOrg.getId()), any(CreateAiSystemRequest.class), any(AppUser.class));
        }

        @Test
        @DisplayName("should return 400 when name is missing")
        void createAiSystem_missingName_returns400() throws Exception {
            CreateAiSystemRequest request = new CreateAiSystemRequest();
            // name is not set (null/blank)
            request.setDescription("A system without a name");

            mockMvc.perform(post("/ai-systems")
                            .with(user(mockUser)).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Validation failed"));

            verify(aiSystemService, never()).createAiSystem(any(), any(), any());
        }

        @Test
        @DisplayName("should return 402 when subscription limit is reached")
        void createAiSystem_subscriptionLimit_returns402() throws Exception {
            CreateAiSystemRequest request = new CreateAiSystemRequest();
            request.setName("Limit Exceeded System");

            when(aiSystemService.createAiSystem(eq(mockOrg.getId()), any(CreateAiSystemRequest.class), any(AppUser.class)))
                    .thenThrow(new SubscriptionLimitException("AI system limit reached for your subscription plan"));

            mockMvc.perform(post("/ai-systems")
                            .with(user(mockUser)).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isPaymentRequired())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("AI system limit reached for your subscription plan"));
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // PUT /ai-systems/{id}
    // ────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("PUT /ai-systems/{id}")
    class UpdateAiSystem {

        @Test
        @DisplayName("should return 200 when AI system is updated")
        void updateAiSystem_valid_returns200() throws Exception {
            UpdateAiSystemRequest request = new UpdateAiSystemRequest();
            request.setName("Updated AI System");
            request.setDescription("Updated description");
            request.setStatus(AiSystemStatus.ACTIVE);

            AiSystemDto updatedDto = AiSystemDto.builder()
                    .id(systemId)
                    .name("Updated AI System")
                    .description("Updated description")
                    .status("ACTIVE")
                    .riskLevel("HIGH")
                    .complianceScore(0)
                    .complianceStatus("NOT_STARTED")
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            when(aiSystemService.updateAiSystem(eq(systemId), eq(mockOrg.getId()), any(UpdateAiSystemRequest.class), any(AppUser.class)))
                    .thenReturn(updatedDto);

            mockMvc.perform(put("/ai-systems/{id}", systemId)
                            .with(user(mockUser)).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("AI system updated"))
                    .andExpect(jsonPath("$.data.name").value("Updated AI System"));

            verify(aiSystemService).updateAiSystem(eq(systemId), eq(mockOrg.getId()), any(UpdateAiSystemRequest.class), any(AppUser.class));
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // DELETE /ai-systems/{id}
    // ────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("DELETE /ai-systems/{id}")
    class DeleteAiSystem {

        @Test
        @DisplayName("should return 200 when AI system is deleted")
        void deleteAiSystem_valid_returns200() throws Exception {
            doNothing().when(aiSystemService).deleteAiSystem(systemId, mockOrg.getId(), mockUser);

            mockMvc.perform(delete("/ai-systems/{id}", systemId)
                            .with(user(mockUser)).with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("AI system deleted"));

            verify(aiSystemService).deleteAiSystem(eq(systemId), eq(mockOrg.getId()), any(AppUser.class));
        }

        @Test
        @DisplayName("should return 404 when AI system not found for deletion")
        void deleteAiSystem_notFound_returns404() throws Exception {
            UUID unknownId = UUID.randomUUID();

            doThrow(new ResourceNotFoundException("AI System", "id", unknownId))
                    .when(aiSystemService).deleteAiSystem(eq(unknownId), eq(mockOrg.getId()), any(AppUser.class));

            mockMvc.perform(delete("/ai-systems/{id}", unknownId)
                            .with(user(mockUser)).with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("AI System not found with id: '" + unknownId + "'"));
        }
    }
}
