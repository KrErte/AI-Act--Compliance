package com.aiaudit.platform.gpai;

import com.aiaudit.platform.TestDataBuilder;
import com.aiaudit.platform.common.exception.ResourceNotFoundException;
import com.aiaudit.platform.compliance.ObligationStatus;
import com.aiaudit.platform.gpai.dto.CreateGpaiModelRequest;
import com.aiaudit.platform.gpai.dto.GpaiModelDto;
import com.aiaudit.platform.gpai.dto.GpaiObligationDto;
import com.aiaudit.platform.organization.Organization;
import com.aiaudit.platform.organization.OrganizationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GpaiServiceTest {

    @Mock
    private GpaiModelRepository modelRepository;

    @Mock
    private GpaiObligationRepository obligationRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @InjectMocks
    private GpaiService gpaiService;

    // ── listModels ──

    @Test
    void listModels_returnsMappedDtoList() {
        UUID orgId = UUID.randomUUID();
        Organization org = TestDataBuilder.anOrganization().id(orgId).build();
        GpaiModel model1 = TestDataBuilder.aGpaiModel().name("Model A").organization(org).build();
        GpaiModel model2 = TestDataBuilder.aGpaiModel().name("Model B").organization(org).build();

        when(modelRepository.findByOrganizationIdOrderByCreatedAtDesc(orgId))
                .thenReturn(List.of(model1, model2));

        List<GpaiModelDto> result = gpaiService.listModels(orgId);

        assertEquals(2, result.size());
        assertEquals("Model A", result.get(0).getName());
        assertEquals("Model B", result.get(1).getName());
        verify(modelRepository).findByOrganizationIdOrderByCreatedAtDesc(orgId);
    }

    @Test
    void listModels_emptyOrg_returnsEmptyList() {
        UUID orgId = UUID.randomUUID();
        when(modelRepository.findByOrganizationIdOrderByCreatedAtDesc(orgId)).thenReturn(List.of());

        List<GpaiModelDto> result = gpaiService.listModels(orgId);

        assertTrue(result.isEmpty());
    }

    // ── getModel ──

    @Test
    void getModel_found_returnsDto() {
        UUID orgId = UUID.randomUUID();
        UUID modelId = UUID.randomUUID();
        GpaiModel model = TestDataBuilder.aGpaiModel().id(modelId).name("Test Model").build();

        when(modelRepository.findByIdAndOrganizationId(modelId, orgId)).thenReturn(Optional.of(model));

        GpaiModelDto result = gpaiService.getModel(modelId, orgId);

        assertNotNull(result);
        assertEquals("Test Model", result.getName());
        assertEquals(modelId, result.getId());
    }

    @Test
    void getModel_notFound_throwsResourceNotFoundException() {
        UUID orgId = UUID.randomUUID();
        UUID modelId = UUID.randomUUID();

        when(modelRepository.findByIdAndOrganizationId(modelId, orgId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> gpaiService.getModel(modelId, orgId));
    }

    // ── createModel ──

    @Test
    void createModel_noSystemicRisk_creates4Obligations() {
        UUID orgId = UUID.randomUUID();
        Organization org = TestDataBuilder.anOrganization().id(orgId).build();

        CreateGpaiModelRequest request = new CreateGpaiModelRequest();
        request.setName("GPT-Test");
        request.setProvider("OpenAI");
        request.setModelType(GpaiModelType.GENERAL);
        request.setHasSystemicRisk(false);
        request.setTrainingComputeFlops(new BigDecimal("1E24"));
        request.setDescription("A test model");
        request.setVersion("1.0");
        request.setOpenSource(false);

        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));
        when(modelRepository.save(any(GpaiModel.class))).thenAnswer(inv -> {
            GpaiModel m = inv.getArgument(0);
            m.setId(UUID.randomUUID());
            return m;
        });

        GpaiModelDto result = gpaiService.createModel(orgId, request);

        assertNotNull(result);
        assertEquals("GPT-Test", result.getName());
        // 4 base obligations (Article 53)
        verify(obligationRepository, times(4)).save(any(GpaiObligation.class));
    }

    @Test
    void createModel_withSystemicRisk_creates8Obligations() {
        UUID orgId = UUID.randomUUID();
        Organization org = TestDataBuilder.anOrganization().id(orgId).build();

        CreateGpaiModelRequest request = new CreateGpaiModelRequest();
        request.setName("Systemic Model");
        request.setProvider("Provider");
        request.setModelType(GpaiModelType.FOUNDATION);
        request.setHasSystemicRisk(true);
        request.setTrainingComputeFlops(new BigDecimal("1E26"));
        request.setDescription("Systemic risk model");
        request.setVersion("2.0");
        request.setOpenSource(false);

        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));
        when(modelRepository.save(any(GpaiModel.class))).thenAnswer(inv -> {
            GpaiModel m = inv.getArgument(0);
            m.setId(UUID.randomUUID());
            return m;
        });

        GpaiModelDto result = gpaiService.createModel(orgId, request);

        assertNotNull(result);
        assertTrue(result.isHasSystemicRisk());
        // 4 base + 4 systemic risk obligations
        verify(obligationRepository, times(8)).save(any(GpaiObligation.class));

        ArgumentCaptor<GpaiObligation> captor = ArgumentCaptor.forClass(GpaiObligation.class);
        verify(obligationRepository, times(8)).save(captor.capture());
        List<GpaiObligation> saved = captor.getAllValues();
        assertTrue(saved.stream().anyMatch(o -> o.getArticleRef().startsWith("Article 55")));
    }

    // ── deleteModel ──

    @Test
    void deleteModel_success_deletesObligationsAndModel() {
        UUID orgId = UUID.randomUUID();
        UUID modelId = UUID.randomUUID();
        GpaiModel model = TestDataBuilder.aGpaiModel().id(modelId).build();

        when(modelRepository.findByIdAndOrganizationId(modelId, orgId)).thenReturn(Optional.of(model));

        gpaiService.deleteModel(modelId, orgId);

        verify(obligationRepository).deleteAllByGpaiModelId(modelId);
        verify(modelRepository).delete(model);
    }

    // ── getObligations ──

    @Test
    void getObligations_returnsMappedList() {
        UUID modelId = UUID.randomUUID();
        GpaiModel model = TestDataBuilder.aGpaiModel().id(modelId).build();
        GpaiObligation obl1 = GpaiObligation.builder()
                .id(UUID.randomUUID())
                .gpaiModel(model)
                .articleRef("Article 53(1)(a)")
                .title("Technical Documentation")
                .description("Test description")
                .status(ObligationStatus.NOT_STARTED)
                .sortOrder(0)
                .build();
        GpaiObligation obl2 = GpaiObligation.builder()
                .id(UUID.randomUUID())
                .gpaiModel(model)
                .articleRef("Article 53(1)(b)")
                .title("Information for Downstream Providers")
                .description("Test description 2")
                .status(ObligationStatus.IN_PROGRESS)
                .sortOrder(1)
                .build();

        when(obligationRepository.findByGpaiModelIdOrderBySortOrder(modelId))
                .thenReturn(List.of(obl1, obl2));

        List<GpaiObligationDto> result = gpaiService.getObligations(modelId);

        assertEquals(2, result.size());
        assertEquals("Article 53(1)(a)", result.get(0).getArticleRef());
        assertEquals("NOT_STARTED", result.get(0).getStatus());
        assertEquals("IN_PROGRESS", result.get(1).getStatus());
    }

    // ── updateObligationStatus ──

    @Test
    void updateObligationStatus_success_updatesAndReturnsDto() {
        UUID oblId = UUID.randomUUID();
        GpaiObligation obligation = GpaiObligation.builder()
                .id(oblId)
                .gpaiModel(TestDataBuilder.aGpaiModel().build())
                .articleRef("Article 53(1)(a)")
                .title("Technical Documentation")
                .description("Test")
                .status(ObligationStatus.NOT_STARTED)
                .sortOrder(0)
                .build();

        when(obligationRepository.findById(oblId)).thenReturn(Optional.of(obligation));
        when(obligationRepository.save(any(GpaiObligation.class))).thenAnswer(inv -> inv.getArgument(0));

        GpaiObligationDto result = gpaiService.updateObligationStatus(oblId, ObligationStatus.COMPLETED);

        assertEquals("COMPLETED", result.getStatus());
        assertEquals(ObligationStatus.COMPLETED, obligation.getStatus());
        verify(obligationRepository).save(obligation);
    }

    @Test
    void updateObligationStatus_notFound_throwsResourceNotFoundException() {
        UUID oblId = UUID.randomUUID();

        when(obligationRepository.findById(oblId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> gpaiService.updateObligationStatus(oblId, ObligationStatus.COMPLETED));
    }
}
