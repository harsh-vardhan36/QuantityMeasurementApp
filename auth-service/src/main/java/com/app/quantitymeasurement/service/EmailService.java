package com.app.quantitymeasurement.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for sending security-related emails (e.g. Password
 * Reset).
 */
@Service
@Slf4j
public class EmailService {

	@Autowired
	private JavaMailSender mailSender;

	@Value("${spring.mail.username}")
	private String fromEmail;

	/**
	 * Sends a password reset OTP email to the user.
	 *
	 * @param to  the recipient's email address
	 * @param otp the 6-digit OTP code
	 */
	@Async
	public void sendPasswordResetEmail(String to, String otp) {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

			helper.setFrom(fromEmail);
			helper.setTo(to);
			helper.setSubject("Password Reset OTP - Quantity Measurement App");
			helper.setText(buildPasswordResetEmailBody(otp), true);

			mailSender.send(message);
			log.info("Password reset OTP email sent successfully to {}", to);
		} catch (Exception e) {
			log.error("Failed to send password reset email to {}: {}", to, e.getMessage());
		}
	}

	private String buildPasswordResetEmailBody(String otp) {
		return "<html><body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>"
				+ "<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #eee; border-radius: 10px;'>"
				+ "<h2 style='color: #2c3e50; text-align: center;'>Password Reset Request</h2>"
				+ "<p>Hello,</p>"
				+ "<p>We received a request to reset the password for your account on <strong>Quantity Measurement App</strong>.</p>"
				+ "<p>Use the OTP code below to reset your password. This code is valid for <strong>15 minutes</strong>.</p>"
				+ "<div style='text-align: center; margin: 30px 0;'>"
				+ "<span style='display: inline-block; background-color: #f4f4f4; border: 2px dashed #3498db; "
				+ "border-radius: 8px; padding: 15px 30px; font-size: 32px; font-weight: bold; "
				+ "letter-spacing: 8px; color: #2c3e50;'>" + otp + "</span>"
				+ "</div>"
				+ "<p>Enter this code in the app to proceed with resetting your password.</p>"
				+ "<p>If you did not request a password reset, please ignore this email or contact support.</p>"
				+ "<hr style='border: 0; border-top: 1px solid #eee; margin: 20px 0;'>"
				+ "<p style='font-size: 12px; color: #7f8c8d; text-align: center;'>&copy; 2026 Quantity Measurement App. All rights reserved.</p>"
				+ "</div></body></html>";
	}

	@Async
	public void sendRegistrationEmail(String email, String firstName) {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
			helper.setFrom(fromEmail);
			helper.setTo(email);
			helper.setSubject("Welcome to Quantity Measurement App!");
			helper.setText(
				"<html><body style='font-family: Arial, sans-serif; color: #333;'>"
				+ "<div style='max-width:600px; margin:0 auto; padding:20px; border:1px solid #eee; border-radius:10px;'>"
				+ "<h2 style='color:#2c3e50; text-align:center;'>Welcome, " + firstName + "!</h2>"
				+ "<p>Your account has been successfully created on <strong>Quantity Measurement App</strong>.</p>"
				+ "<p>You can now login and start using the application.</p>"
				+ "<hr style='border:0; border-top:1px solid #eee; margin:20px 0;'>"
				+ "<p style='font-size:12px; color:#7f8c8d; text-align:center;'>&copy; 2026 Quantity Measurement App. All rights reserved.</p>"
				+ "</div></body></html>", true);
			mailSender.send(message);
			log.info("Welcome email sent to {}", email);
		} catch (Exception e) {
			log.error("Failed to send welcome email to {}: {}", email, e.getMessage());
		}
	}

	@Async
	public void sendLoginNotificationEmail(String email, String firstName) {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
			helper.setFrom(fromEmail);
			helper.setTo(email);
			helper.setSubject("New Login Detected - Quantity Measurement App");
			helper.setText(
				"<html><body style='font-family: Arial, sans-serif; color: #333;'>"
				+ "<div style='max-width:600px; margin:0 auto; padding:20px; border:1px solid #eee; border-radius:10px;'>"
				+ "<h2 style='color:#2c3e50; text-align:center;'>Login Alert</h2>"
				+ "<p>Hi " + firstName + ",</p>"
				+ "<p>A new login to your <strong>Quantity Measurement App</strong> account was detected.</p>"
				+ "<p>If this was not you, please reset your password immediately.</p>"
				+ "<hr style='border:0; border-top:1px solid #eee; margin:20px 0;'>"
				+ "<p style='font-size:12px; color:#7f8c8d; text-align:center;'>&copy; 2026 Quantity Measurement App. All rights reserved.</p>"
				+ "</div></body></html>", true);
			mailSender.send(message);
			log.info("Login notification email sent to {}", email);
		} catch (Exception e) {
			log.error("Failed to send login notification to {}: {}", email, e.getMessage());
		}
	}

}
