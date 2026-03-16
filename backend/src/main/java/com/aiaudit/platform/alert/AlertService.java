package com.aiaudit.platform.alert;

import com.aiaudit.platform.alert.dto.AlertDto;
import com.aiaudit.platform.common.PagedResponse;
import com.aiaudit.platform.common.exception.ResourceNotFoundException;
import com.aiaudit.platform.organization.Organization;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final ComplianceAlertRepository alertRepository;

    @Transactional(readOnly = true)
    public PagedResponse<AlertDto> getAlerts(UUID organizationId, int page, int size) {
        Page<AlertDto> result = alertRepository
                .findByOrganizationIdAndDismissedFalseOrderByCreatedAtDesc(organizationId, PageRequest.of(page, size))
                .map(AlertDto::from);
        return PagedResponse.from(result);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(UUID organizationId) {
        return alertRepository.countByOrganizationIdAndReadFalseAndDismissedFalse(organizationId);
    }

    @Transactional
    public void markAsRead(UUID alertId) {
        ComplianceAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert", "id", alertId));
        alert.setRead(true);
        alertRepository.save(alert);
    }

    @Transactional
    public void markAllAsRead(UUID organizationId) {
        alertRepository.markAllAsRead(organizationId);
    }

    @Transactional
    public void dismissAlert(UUID alertId) {
        ComplianceAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert", "id", alertId));
        alert.setDismissed(true);
        alertRepository.save(alert);
    }

    @Transactional
    public void createAlert(Organization org, AlertType type, AlertSeverity severity,
                            String title, String message) {
        ComplianceAlert alert = ComplianceAlert.builder()
                .organization(org)
                .alertType(type)
                .severity(severity)
                .title(title)
                .message(message)
                .source("SYSTEM")
                .build();
        alertRepository.save(alert);
    }
}
