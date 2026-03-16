package com.aiaudit.platform.compliance.dto;

import com.aiaudit.platform.compliance.ObligationStatus;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class UpdateObligationRequest {
    private ObligationStatus status;
    private UUID assignedTo;
    private LocalDate dueDate;
    private String notes;
}
