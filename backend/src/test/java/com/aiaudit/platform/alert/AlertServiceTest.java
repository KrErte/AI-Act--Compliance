package com.aiaudit.platform.alert;

import com.aiaudit.platform.TestDataBuilder;
import com.aiaudit.platform.alert.dto.AlertDto;
import com.aiaudit.platform.common.PagedResponse;
import com.aiaudit.platform.common.exception.ResourceNotFoundException;
import com.aiaudit.platform.organization.Organization;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private ComplianceAlertRepository alertRepository;

    @InjectMocks
    private AlertService alertService;

    // ── getAlerts ──

    @Test
    void getAlerts_returnsPagedResponse() {
        UUID orgId = UUID.randomUUID();
        Organization org = TestDataBuilder.anOrganization().id(orgId).build();
        ComplianceAlert alert1 = TestDataBuilder.anAlert().organization(org).title("Alert 1").build();
        ComplianceAlert alert2 = TestDataBuilder.anAlert().organization(org).title("Alert 2").build();

        Page<ComplianceAlert> page = new PageImpl<>(
                List.of(alert1, alert2),
                PageRequest.of(0, 10),
                2
        );
        when(alertRepository.findByOrganizationIdAndDismissedFalseOrderByCreatedAtDesc(
                eq(orgId), any(PageRequest.class))).thenReturn(page);

        PagedResponse<AlertDto> result = alertService.getAlerts(orgId, 0, 10);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalElements());
        assertEquals("Alert 1", result.getContent().get(0).getTitle());
        assertEquals("Alert 2", result.getContent().get(1).getTitle());
        assertTrue(result.isLast());
    }

    // ── getUnreadCount ──

    @Test
    void getUnreadCount_returnsCount() {
        UUID orgId = UUID.randomUUID();
        when(alertRepository.countByOrganizationIdAndReadFalseAndDismissedFalse(orgId)).thenReturn(5L);

        long count = alertService.getUnreadCount(orgId);

        assertEquals(5L, count);
        verify(alertRepository).countByOrganizationIdAndReadFalseAndDismissedFalse(orgId);
    }

    // ── markAsRead ──

    @Test
    void markAsRead_success_setsReadTrue() {
        UUID alertId = UUID.randomUUID();
        ComplianceAlert alert = TestDataBuilder.anAlert().id(alertId).read(false).build();

        when(alertRepository.findById(alertId)).thenReturn(Optional.of(alert));

        alertService.markAsRead(alertId);

        assertTrue(alert.isRead());
        verify(alertRepository).save(alert);
    }

    @Test
    void markAsRead_notFound_throwsResourceNotFoundException() {
        UUID alertId = UUID.randomUUID();
        when(alertRepository.findById(alertId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> alertService.markAsRead(alertId));

        verify(alertRepository, never()).save(any());
    }

    // ── dismissAlert ──

    @Test
    void dismissAlert_success_setsDismissedTrue() {
        UUID alertId = UUID.randomUUID();
        ComplianceAlert alert = TestDataBuilder.anAlert().id(alertId).dismissed(false).build();

        when(alertRepository.findById(alertId)).thenReturn(Optional.of(alert));

        alertService.dismissAlert(alertId);

        assertTrue(alert.isDismissed());
        verify(alertRepository).save(alert);
    }

    @Test
    void dismissAlert_notFound_throwsResourceNotFoundException() {
        UUID alertId = UUID.randomUUID();
        when(alertRepository.findById(alertId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> alertService.dismissAlert(alertId));

        verify(alertRepository, never()).save(any());
    }

    // ── createAlert ──

    @Test
    void createAlert_savesAlertWithCorrectFields() {
        Organization org = TestDataBuilder.anOrganization().build();

        alertService.createAlert(org, AlertType.DEADLINE, AlertSeverity.HIGH,
                "Deadline approaching", "Your compliance deadline is in 30 days");

        ArgumentCaptor<ComplianceAlert> captor = ArgumentCaptor.forClass(ComplianceAlert.class);
        verify(alertRepository).save(captor.capture());

        ComplianceAlert saved = captor.getValue();
        assertEquals(org, saved.getOrganization());
        assertEquals(AlertType.DEADLINE, saved.getAlertType());
        assertEquals(AlertSeverity.HIGH, saved.getSeverity());
        assertEquals("Deadline approaching", saved.getTitle());
        assertEquals("Your compliance deadline is in 30 days", saved.getMessage());
        assertEquals("SYSTEM", saved.getSource());
    }
}
