package com.aiaudit.platform.regulation;

import com.aiaudit.platform.common.exception.ResourceNotFoundException;
import com.aiaudit.platform.regulation.dto.RegulationDetailDto;
import com.aiaudit.platform.regulation.dto.RegulationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegulationService {

    private final RegulationRepository regulationRepository;
    private final RegulationQuestionRepository questionRepository;

    @Transactional(readOnly = true)
    public List<RegulationDto> getAllRegulations() {
        return regulationRepository.findAll().stream()
                .map(r -> {
                    int qCount = r.getDomains().stream()
                            .mapToInt(d -> d.getQuestions().size())
                            .sum();
                    return RegulationDto.from(r, qCount);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public RegulationDetailDto getRegulationDetail(UUID id) {
        Regulation regulation = regulationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Regulation not found"));
        return RegulationDetailDto.from(regulation);
    }

    @Transactional(readOnly = true)
    public RegulationDetailDto getRegulationByCode(String code) {
        Regulation regulation = regulationRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Regulation not found: " + code));
        return RegulationDetailDto.from(regulation);
    }
}
