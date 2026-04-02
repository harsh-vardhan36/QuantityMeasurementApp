package com.app.quantitymeasurement.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for sending transactional emails via SMTP (JavaMail).
 * All send operations are executed asynchronously to avoid blocking the request thread.
 */
@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Sends a welcome email after successful registration.
     *
     * @param to        the recipient's email address
     * @param firstName the user's first name for personalisation
     */
    @Async
    public void sendRegistrationEmail(String to, String firstName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Welcome to Quantity Measurement App!");
            helper.setText(buildRegistrationEmailBody(firstName), true);

            mailSender.send(message);
            log.info("Registration email sent to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send registration email to {}: {}", to, e.getMessage());
        }
    }

    /**
     * Sends a password-reset OTP email.
     *
     * @param to  the recipient's email address
     * @param otp the one-time password to include in the email
     */
    @Async
    public void sendPasswordResetEmail(String to, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Password Reset OTP – Quantity Measurement App");
            helper.setText(buildPasswordResetEmailBody(otp), true);

            mailSender.send(message);
            log.info("Password reset OTP email sent to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to {}: {}", to, e.getMessage());
        }
    }

    /**
     * Sends a login notification email (optional security alert).
     *
     * @param to        the recipient's email address
     * @param firstName the user's first name
     */
    @Async
    public void sendLoginNotificationEmail(String to, String firstName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("New Login Detected – Quantity Measurement App");
            helper.setText(buildLoginNotificationBody(firstName), true);

            mailSender.send(message);
            log.info("Login notification email sent to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send login notification email to {}: {}", to, e.getMessage());
        }
    }

    // ── Email Body Builders ────────────────────────────────────────────────────

    private String buildRegistrationEmailBody(String firstName) {
        return "<html><body style='font-family:Arial,sans-serif;'>"
                + "<h2 style='color:#4CAF50;'>Welcome, " + firstName + "!</h2>"
                + "<p>Your account has been successfully created on <strong>Quantity Measurement App</strong>.</p>"
                + "<p>You can now log in and start using all features.</p>"
                + "<br/><p style='color:#888;font-size:12px;'>If you did not register, please ignore this email.</p>"
                + "</body></html>";
    }

    private String buildPasswordResetEmailBody(String otp) {
        return "<html><body style='font-family:Arial,sans-serif;'>"
                + "<h2 style='color:#FF5722;'>Password Reset Request</h2>"
                + "<p>We received a request to reset your password. Use the OTP below:</p>"
                + "<div style='background:#f4f4f4;padding:15px;border-radius:8px;text-align:center;"
                + "font-size:32px;font-weight:bold;letter-spacing:8px;color:#333;'>"
                + otp + "</div>"
                + "<p style='margin-top:15px;'>This OTP is valid for <strong>15 minutes</strong>.</p>"
                + "<p style='color:#888;font-size:12px;'>If you did not request a password reset, please ignore this email.</p>"
                + "</body></html>";
    }

    private String buildLoginNotificationBody(String firstName) {
        return "<html><body style='font-family:Arial,sans-serif;'>"
                + "<h2 style='color:#2196F3;'>Login Notification</h2>"
                + "<p>Hi <strong>" + firstName + "</strong>,</p>"
                + "<p>A new login was detected on your <strong>Quantity Measurement App</strong> account.</p>"
                + "<p>If this was you, no action is needed. If not, please reset your password immediately.</p>"
                + "</body></html>";
    }
}
