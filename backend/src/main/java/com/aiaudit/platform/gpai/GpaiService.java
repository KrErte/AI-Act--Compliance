package com.aiaudit.platform.gpai;

import com.aiaudit.platform.common.exception.ResourceNotFoundException;
import com.aiaudit.platform.compliance.ObligationStatus;
import com.aiaudit.platform.gpai.dto.*;
import com.aiaudit.platform.organization.Organization;
import com.aiaudit.platform.organization.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class GpaiService {

    private final GpaiModelRepository modelRepository;
    private final GpaiObligationRepository obligationRepository;
    private final OrganizationRepository organizationRepository;

    @Transactional(readOnly = true)
    public List<GpaiModelDto> listModels(UUID organizationId) {
        return modelRepository.findByOrganizationIdOrderByCreatedAtDesc(organizationId).stream()
                .map(GpaiModelDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public GpaiModelDto getModel(UUID modelId, UUID organizationId) {
        GpaiModel model = findByIdAndOrg(modelId, organizationId);
        return GpaiModelDto.from(model);
    }

    @Transactional
    public GpaiModelDto createModel(UUID organizationId, CreateGpaiModelRequest request) {
        Organization org = organizationRepository.findById(organizationId).orElseThrow();

        GpaiModel model = GpaiModel.builder()
                .name(request.getName())
                .provider(request.getProvider())
                .modelType(request.getModelType())
                .hasSystemicRisk(request.isHasSystemicRisk())
                .trainingComputeFlops(request.getTrainingComputeFlops())
                .description(request.getDescription())
                .version(request.getVersion())
                .openSource(request.isOpenSource())
                .organization(org)
                .build();

        model = modelRepository.save(model);
        generateGpaiObligations(model);
        return GpaiModelDto.from(model);
    }

    @Transactional
    public void deleteModel(UUID modelId, UUID organizationId) {
        GpaiModel model = findByIdAndOrg(modelId, organizationId);
        obligationRepository.deleteAllByGpaiModelId(modelId);
        modelRepository.delete(model);
    }

    @Transactional(readOnly = true)
    public List<GpaiObligationDto> getObligations(UUID modelId) {
        return obligationRepository.findByGpaiModelIdOrderBySortOrder(modelId).stream()
                .map(GpaiObligationDto::from)
                .toList();
    }

    @Transactional
    public GpaiObligationDto updateObligationStatus(UUID obligationId, ObligationStatus status) {
        GpaiObligation obligation = obligationRepository.findById(obligationId)
                .orElseThrow(() -> new ResourceNotFoundException("GPAI Obligation", "id", obligationId));
        obligation.setStatus(status);
        return GpaiObligationDto.from(obligationRepository.save(obligation));
    }

    private GpaiModel findByIdAndOrg(UUID modelId, UUID organizationId) {
        return modelRepository.findByIdAndOrganizationId(modelId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("GPAI Model", "id", modelId));
    }

    private void generateGpaiObligations(GpaiModel model) {
        AtomicInteger order = new AtomicInteger(0);

        // All GPAI models - Article 53
        createObligation(model, "Article 53(1)(a)", "Technical Documentation",
                "Draw up and keep up-to-date technical documentation of the model.", order.getAndIncrement());
        createObligation(model, "Article 53(1)(b)", "Information for Downstream Providers",
                "Provide information and documentation to downstream AI system providers.", order.getAndIncrement());
        createObligation(model, "Article 53(1)(c)", "Copyright Policy",
                "Put in place a policy to comply with EU copyright law.", order.getAndIncrement());
        createObligation(model, "Article 53(1)(d)", "Training Data Summary",
                "Draw up and make publicly available a sufficiently detailed summary of training data.", order.getAndIncrement());

        // Systemic risk models - Article 55
        if (model.isHasSystemicRisk()) {
            createObligation(model, "Article 55(1)(a)", "Model Evaluation",
                    "Perform model evaluation including adversarial testing.", order.getAndIncrement());
            createObligation(model, "Article 55(1)(b)", "Systemic Risk Assessment",
                    "Assess and mitigate possible systemic risks.", order.getAndIncrement());
            createObligation(model, "Article 55(1)(c)", "Incident Tracking",
                    "Track, document and report serious incidents to the AI Office.", order.getAndIncrement());
            createObligation(model, "Article 55(1)(d)", "Cybersecurity Protection",
                    "Ensure adequate level of cybersecurity protection.", order.getAndIncrement());
        }

        // Open source exemptions
        if (model.isOpenSource() && !model.isHasSystemicRisk()) {
            // Open source models without systemic risk have fewer obligations
            // They are still required to comply with Article 53(1)(c) and (d) only
        }
    }

    private void createObligation(GpaiModel model, String ref, String title, String desc, int order) {
        GpaiObligation obligation = GpaiObligation.builder()
                .gpaiModel(model)
                .articleRef(ref)
                .title(title)
                .description(desc)
                .sortOrder(order)
                .build();
        obligationRepository.save(obligation);
    }
}
