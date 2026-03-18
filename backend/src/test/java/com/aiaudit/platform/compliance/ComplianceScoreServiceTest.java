package com.aiaudit.platform.compliance;

import com.aiaudit.platform.aisystem.AiSystem;
import com.aiaudit.platform.aisystem.AiSystemRepository;
import com.aiaudit.platform.aisystem.ComplianceStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ComplianceScoreServiceTest {

    @Mock
    private ComplianceObligationRepository obligationRepository;

    @Mock
    private AiSystemRepository aiSystemRepository;

    @InjectMocks
    private ComplianceScoreService complianceScoreService;

    @Test
    void recalculate_allCompleted_score100() {
        UUID systemId = UUID.randomUUID();
        AiSystem system = AiSystem.builder().id(systemId).build();

        when(obligationRepository.countByAiSystemId(systemId)).thenReturn(5L);
        when(obligationRepository.countByAiSystemIdAndStatus(systemId, ObligationStatus.COMPLETED)).thenReturn(5L);
        when(obligationRepository.countByAiSystemIdAndStatus(systemId, ObligationStatus.NOT_APPLICABLE)).thenReturn(0L);
        when(aiSystemRepository.findById(systemId)).thenReturn(Optional.of(system));

        complianceScoreService.recalculate(systemId);

        assertEquals(100, system.getComplianceScore());
        assertEquals(ComplianceStatus.COMPLIANT, system.getComplianceStatus());
        verify(aiSystemRepository).save(system);
    }

    @Test
    void recalculate_noneCompleted_score0() {
        UUID systemId = UUID.randomUUID();
        AiSystem system = AiSystem.builder().id(systemId).build();

        when(obligationRepository.countByAiSystemId(systemId)).thenReturn(10L);
        when(obligationRepository.countByAiSystemIdAndStatus(systemId, ObligationStatus.COMPLETED)).thenReturn(0L);
        when(obligationRepository.countByAiSystemIdAndStatus(systemId, ObligationStatus.NOT_APPLICABLE)).thenReturn(0L);
        when(obligationRepository.countByAiSystemIdAndStatus(systemId, ObligationStatus.IN_PROGRESS)).thenReturn(0L);
        when(aiSystemRepository.findById(systemId)).thenReturn(Optional.of(system));

        complianceScoreService.recalculate(systemId);

        assertEquals(0, system.getComplianceScore());
        assertEquals(ComplianceStatus.NOT_STARTED, system.getComplianceStatus());
    }

    @Test
    void recalculate_partialCompletion_correctScore() {
        UUID systemId = UUID.randomUUID();
        AiSystem system = AiSystem.builder().id(systemId).build();

        when(obligationRepository.countByAiSystemId(systemId)).thenReturn(10L);
        when(obligationRepository.countByAiSystemIdAndStatus(systemId, ObligationStatus.COMPLETED)).thenReturn(3L);
        when(obligationRepository.countByAiSystemIdAndStatus(systemId, ObligationStatus.NOT_APPLICABLE)).thenReturn(0L);
        when(aiSystemRepository.findById(systemId)).thenReturn(Optional.of(system));

        complianceScoreService.recalculate(systemId);

        // 3 completed out of 10 applicable = 30%
        assertEquals(30, system.getComplianceScore());
        // completed > 0 so short-circuits to IN_PROGRESS
        assertEquals(ComplianceStatus.IN_PROGRESS, system.getComplianceStatus());
    }

    @Test
    void recalculate_withNotApplicable_excludesFromTotal() {
        UUID systemId = UUID.randomUUID();
        AiSystem system = AiSystem.builder().id(systemId).build();

        when(obligationRepository.countByAiSystemId(systemId)).thenReturn(10L);
        when(obligationRepository.countByAiSystemIdAndStatus(systemId, ObligationStatus.COMPLETED)).thenReturn(4L);
        when(obligationRepository.countByAiSystemIdAndStatus(systemId, ObligationStatus.NOT_APPLICABLE)).thenReturn(2L);
        when(aiSystemRepository.findById(systemId)).thenReturn(Optional.of(system));

        complianceScoreService.recalculate(systemId);

        // 4 completed out of 8 applicable = 50%
        assertEquals(50, system.getComplianceScore());
    }

    @Test
    void recalculate_noObligations_noUpdate() {
        UUID systemId = UUID.randomUUID();
        when(obligationRepository.countByAiSystemId(systemId)).thenReturn(0L);

        complianceScoreService.recalculate(systemId);

        verify(aiSystemRepository, never()).findById(any());
        verify(aiSystemRepository, never()).save(any());
    }
}
