package com.aiaudit.platform.audit;

import com.aiaudit.platform.TestDataBuilder;
import com.aiaudit.platform.audit.dto.AuditLogDto;
import com.aiaudit.platform.auth.AppUser;
import com.aiaudit.platform.common.PagedResponse;
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
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditService auditService;

    // ── log (full parameters) ──

    @Test
    void log_fullParams_savesAuditLogWithAllFields() {
        Organization org = TestDataBuilder.anOrganization().build();
        AppUser user = TestDataBuilder.anAppUser().organization(org).build();
        UUID entityId = UUID.randomUUID();
        Map<String, Object> metadata = Map.of("key", "value");

        auditService.log("AiSystem", entityId, "UPDATE",
                "{\"name\":\"Old\"}", "{\"name\":\"New\"}", metadata, user, org);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertEquals("AiSystem", saved.getEntityType());
        assertEquals(entityId, saved.getEntityId());
        assertEquals("UPDATE", saved.getAction());
        assertEquals("{\"name\":\"Old\"}", saved.getOldValue());
        assertEquals("{\"name\":\"New\"}", saved.getNewValue());
        assertEquals(metadata, saved.getMetadata());
        assertEquals(user, saved.getUser());
        assertEquals(org, saved.getOrganization());
    }

    @Test
    void log_exceptionSwallowed_doesNotPropagate() {
        Organization org = TestDataBuilder.anOrganization().build();
        AppUser user = TestDataBuilder.anAppUser().organization(org).build();
        UUID entityId = UUID.randomUUID();

        when(auditLogRepository.save(any(AuditLog.class))).thenThrow(new RuntimeException("DB error"));

        // Should not throw -- the service catches and logs the exception
        assertDoesNotThrow(() ->
                auditService.log("AiSystem", entityId, "CREATE", null, null, null, user, org));
    }

    // ── log (short overload) ──

    @Test
    void log_shortOverload_delegatesWithNulls() {
        Organization org = TestDataBuilder.anOrganization().build();
        AppUser user = TestDataBuilder.anAppUser().organization(org).build();
        UUID entityId = UUID.randomUUID();

        auditService.log("AiSystem", entityId, "DELETE", user, org);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertEquals("AiSystem", saved.getEntityType());
        assertEquals(entityId, saved.getEntityId());
        assertEquals("DELETE", saved.getAction());
        assertNull(saved.getOldValue());
        assertNull(saved.getNewValue());
        assertNull(saved.getMetadata());
        assertEquals(user, saved.getUser());
        assertEquals(org, saved.getOrganization());
    }

    // ── getAuditLog ──

    @Test
    void getAuditLog_returnsPagedResponse() {
        UUID orgId = UUID.randomUUID();
        Organization org = TestDataBuilder.anOrganization().id(orgId).build();
        AppUser user = TestDataBuilder.anAppUser().organization(org).build();
        AuditLog log1 = TestDataBuilder.anAuditLog()
                .organization(org).user(user).action("CREATE").build();
        AuditLog log2 = TestDataBuilder.anAuditLog()
                .organization(org).user(user).action("UPDATE").build();

        Page<AuditLog> page = new PageImpl<>(
                List.of(log1, log2),
                PageRequest.of(0, 20),
                2
        );
        when(auditLogRepository.findByOrganizationIdOrderByCreatedAtDesc(
                eq(orgId), any(PageRequest.class))).thenReturn(page);

        PagedResponse<AuditLogDto> result = auditService.getAuditLog(orgId, 0, 20);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalElements());
        assertEquals("CREATE", result.getContent().get(0).getAction());
        assertEquals("UPDATE", result.getContent().get(1).getAction());
        assertTrue(result.isLast());
    }

    // ── getEntityAuditLog ──

    @Test
    void getEntityAuditLog_returnsPagedResponseForEntity() {
        UUID entityId = UUID.randomUUID();
        String entityType = "GpaiModel";
        Organization org = TestDataBuilder.anOrganization().build();
        AppUser user = TestDataBuilder.anAppUser().organization(org).build();
        AuditLog log1 = TestDataBuilder.anAuditLog()
                .entityType(entityType).entityId(entityId)
                .organization(org).user(user).action("CREATE").build();

        Page<AuditLog> page = new PageImpl<>(
                List.of(log1),
                PageRequest.of(0, 10),
                1
        );
        when(auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
                eq(entityType), eq(entityId), any(PageRequest.class))).thenReturn(page);

        PagedResponse<AuditLogDto> result = auditService.getEntityAuditLog(entityType, entityId, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(entityType, result.getContent().get(0).getEntityType());
        assertEquals(entityId, result.getContent().get(0).getEntityId());
        assertEquals(1, result.getTotalElements());
    }
}
