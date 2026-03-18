package com.aiaudit.platform.auth;

import com.aiaudit.platform.email.EmailTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EmailService {

    private final WebClient resendClient;
    private final EmailTemplateService templateService;
    private final String fromAddress;
    private final String frontendUrl;

    public EmailService(
            @Value("${resend.api-key:}") String apiKey,
            @Value("${mail.from}") String fromAddress,
            @Value("${app.frontend-url:https://app.aiaudit.eu}") String frontendUrl,
            EmailTemplateService templateService) {
        this.fromAddress = fromAddress;
        this.frontendUrl = frontendUrl;
        this.templateService = templateService;
        this.resendClient = WebClient.builder()
                .baseUrl("https://api.resend.com")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String token) {
        String resetUrl = frontendUrl + "/auth/reset-password?token=" + token;
        String html = templateService.renderPasswordReset(resetUrl);
        sendEmail(toEmail, "AIAudit — Password Reset", html);
    }

    @Async
    public void sendTeamInvitationEmail(String toEmail, String token, String organizationName, String inviterName) {
        String acceptUrl = frontendUrl + "/auth/accept-invitation?token=" + token;
        String html = templateService.renderTeamInvitation(inviterName, organizationName, acceptUrl);
        sendEmail(toEmail, "AIAudit — You've been invited to join " + organizationName, html);
    }

    @Async
    public void sendTaskAssignmentEmail(String toEmail, String taskTitle, String aiSystemName) {
        String tasksUrl = frontendUrl + "/my-tasks";
        String html = templateService.renderTaskAssignment(taskTitle, aiSystemName, tasksUrl);
        sendEmail(toEmail, "AIAudit — New task assigned: " + taskTitle, html);
    }

    @Async
    public void sendDeadlineAlertEmail(String toEmail, String obligationTitle, String aiSystemName, String dueDate, int daysLeft) {
        String dashboardUrl = frontendUrl + "/dashboard";
        String html = templateService.renderDeadlineAlert(obligationTitle, aiSystemName, dueDate, daysLeft, dashboardUrl);
        String subject = daysLeft <= 0
                ? "AIAudit — OVERDUE: " + obligationTitle
                : "AIAudit — Deadline in " + daysLeft + " days: " + obligationTitle;
        sendEmail(toEmail, subject, html);
    }

    @Async
    public void sendWeeklyDigestEmail(String toEmail, String userName, int totalSystems, int complianceScore,
                                       int completedThisWeek, int overdueCount, List<String> upcomingDeadlines) {
        String dashboardUrl = frontendUrl + "/dashboard";
        String html = templateService.renderWeeklyDigest(userName, totalSystems, complianceScore,
                completedThisWeek, overdueCount, upcomingDeadlines, dashboardUrl);
        sendEmail(toEmail, "AIAudit — Weekly Compliance Digest", html);
    }

    private void sendEmail(String to, String subject, String html) {
        try {
            Map<String, Object> payload = Map.of(
                    "from", fromAddress,
                    "to", List.of(to),
                    "subject", subject,
                    "html", html
            );
            resendClient.post()
                    .uri("/emails")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .subscribe(
                            response -> log.info("Email sent to {}: {}", to, subject),
                            error -> log.error("Failed to send email to {}: {}", to, error.getMessage())
                    );
        } catch (Exception e) {
            log.error("Failed to send email to {}", to, e);
        }
    }
}
