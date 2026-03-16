package com.aiaudit.platform.compliance;

import com.aiaudit.platform.aisystem.AiSystem;
import com.aiaudit.platform.aisystem.AiSystemRepository;
import com.aiaudit.platform.aisystem.ComplianceStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ComplianceScoreService {

    private final ComplianceObligationRepository obligationRepository;
    private final AiSystemRepository aiSystemRepository;

    @Transactional
    public void recalculate(UUID aiSystemId) {
        long total = obligationRepository.countByAiSystemId(aiSystemId);
        if (total == 0) return;

        long completed = obligationRepository.countByAiSystemIdAndStatus(aiSystemId, ObligationStatus.COMPLETED);
        long notApplicable = obligationRepository.countByAiSystemIdAndStatus(aiSystemId, ObligationStatus.NOT_APPLICABLE);
        long applicable = total - notApplicable;

        int score = applicable > 0 ? (int) ((completed * 100) / applicable) : 100;

        AiSystem system = aiSystemRepository.findById(aiSystemId).orElseThrow();
        system.setComplianceScore(score);

        if (score == 100) {
            system.setComplianceStatus(ComplianceStatus.COMPLIANT);
        } else if (completed > 0 || obligationRepository.countByAiSystemIdAndStatus(aiSystemId, ObligationStatus.IN_PROGRESS) > 0) {
            system.setComplianceStatus(ComplianceStatus.IN_PROGRESS);
        } else {
            system.setComplianceStatus(ComplianceStatus.NOT_STARTED);
        }

        aiSystemRepository.save(system);
    }
}
