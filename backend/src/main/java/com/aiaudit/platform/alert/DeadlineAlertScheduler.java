package com.aiaudit.platform.alert;

import com.aiaudit.platform.compliance.ComplianceObligation;
import com.aiaudit.platform.compliance.ComplianceObligationRepository;
import com.aiaudit.platform.compliance.ObligationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeadlineAlertScheduler {

    private final ComplianceObligationRepository obligationRepository;
    private final AlertService alertService;

    @Scheduled(cron = "0 0 8 * * *") // Daily at 8 AM
    @Transactional
    public void checkDeadlines() {
        log.info("Running deadline alert check");
        LocalDate today = LocalDate.now();
        LocalDate warningDate = today.plusDays(14);

        List<ComplianceObligation> upcoming = obligationRepository.findAll().stream()
                .filter(o -> o.getDueDate() != null)
                .filter(o -> o.getStatus() != ObligationStatus.COMPLETED && o.getStatus() != ObligationStatus.NOT_APPLICABLE)
                .filter(o -> !o.getDueDate().isAfter(warningDate))
                .toList();

        for (ComplianceObligation obligation : upcoming) {
            AlertSeverity severity;
            String title;

            if (obligation.getDueDate().isBefore(today)) {
                severity = AlertSeverity.CRITICAL;
                title = "OVERDUE: " + obligation.getArticleTitle();
            } else if (obligation.getDueDate().isBefore(today.plusDays(3))) {
                severity = AlertSeverity.HIGH;
                title = "Due in " + today.until(obligation.getDueDate()).getDays() + " days: " + obligation.getArticleTitle();
            } else {
                severity = AlertSeverity.MEDIUM;
                title = "Due in " + today.until(obligation.getDueDate()).getDays() + " days: " + obligation.getArticleTitle();
            }

            alertService.createAlert(
                    obligation.getAiSystem().getOrganization(),
                    AlertType.DEADLINE,
                    severity,
                    title,
                    "Obligation '" + obligation.getArticleTitle() + "' for AI system '" +
                            obligation.getAiSystem().getName() + "' is due on " + obligation.getDueDate()
            );
        }
        log.info("Created {} deadline alerts", upcoming.size());
    }
}
