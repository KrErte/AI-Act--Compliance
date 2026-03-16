package com.aiaudit.platform.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${mail.from}")
    private String fromAddress;

    @Async
    public void sendPasswordResetEmail(String toEmail, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(toEmail);
            message.setSubject("AIAudit - Password Reset");
            message.setText(
                    "You requested a password reset for your AIAudit account.\n\n" +
                    "Click the link below to reset your password:\n" +
                    "https://app.aiaudit.eu/auth/reset-password?token=" + token + "\n\n" +
                    "This link expires in 1 hour.\n\n" +
                    "If you didn't request this, please ignore this email."
            );
            mailSender.send(message);
            log.info("Password reset email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}", toEmail, e);
        }
    }

    @Async
    public void sendTeamInvitationEmail(String toEmail, String token, String organizationName, String inviterName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(toEmail);
            message.setSubject("AIAudit - You've been invited to join " + organizationName);
            message.setText(
                    inviterName + " has invited you to join " + organizationName + " on AIAudit.\n\n" +
                    "Click the link below to accept the invitation and create your account:\n" +
                    "https://app.aiaudit.eu/auth/accept-invitation?token=" + token + "\n\n" +
                    "This invitation expires in 7 days.\n\n" +
                    "If you didn't expect this invitation, please ignore this email."
            );
            mailSender.send(message);
            log.info("Team invitation email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send team invitation email to {}", toEmail, e);
        }
    }

    @Async
    public void sendTaskAssignmentEmail(String toEmail, String taskTitle, String aiSystemName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(toEmail);
            message.setSubject("AIAudit - New task assigned: " + taskTitle);
            message.setText(
                    "You have been assigned a new compliance task on AIAudit.\n\n" +
                    "Task: " + taskTitle + "\n" +
                    "AI System: " + aiSystemName + "\n\n" +
                    "Log in to AIAudit to view the details and take action:\n" +
                    "https://app.aiaudit.eu/my-tasks"
            );
            mailSender.send(message);
            log.info("Task assignment email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send task assignment email to {}", toEmail, e);
        }
    }
}
