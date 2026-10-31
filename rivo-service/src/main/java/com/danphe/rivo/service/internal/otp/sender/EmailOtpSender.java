package com.danphe.rivo.service.internal.otp.sender;

import com.danphe.rivo.common.config.AppConfig;
import com.danphe.rivo.common.library.base.exception.AppException;
import com.danphe.rivo.common.otp.OtpPurpose;
import com.danphe.rivo.service.api.OtpSender;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@Primary
public class EmailOtpSender implements OtpSender {

  private static final Logger log = LoggerFactory.getLogger(EmailOtpSender.class);

  private final AppConfig appConfig;
  private final JavaMailSender mailSender;
  private final LoggingOtpSender loggingOtpSender;

  public EmailOtpSender(
      AppConfig appConfig,
      JavaMailSender mailSender,
      LoggingOtpSender loggingOtpSender) {
    this.appConfig = appConfig;
    this.mailSender = mailSender;
    this.loggingOtpSender = loggingOtpSender;
  }

  @Override
  public void sendOtp(String identifier, String plainOtp, OtpPurpose purpose) {
    if (!appConfig.isEmailConfigured()) {
      loggingOtpSender.sendOtp(identifier, plainOtp, purpose);
      return;
    }

    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      helper.setFrom(appConfig.getEmailFrom());
      helper.setTo(identifier);
      helper.setSubject("Rivo - Verification Code");
      helper.setText(buildHtmlEmail(plainOtp), true);
      mailSender.send(message);
      log.info("OTP verification email sent to {}", identifier);
    } catch (Exception e) {
      log.error("Failed to send OTP email to {}: {}", identifier, e.getMessage(), e);
      throw new AppException("Failed to dispatch verification email: " + e.getMessage());
    }
  }

  private String buildHtmlEmail(String otp) {
    return "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 24px; border: 1px solid #e2e8f0; border-radius: 8px; background-color: #ffffff;\">"
        + "<h2 style=\"color: #0f172a; margin-top: 0;\">Verify Your Account</h2>"
        + "<p style=\"color: #334155; font-size: 15px; line-height: 1.5;\">Please use the verification code below to complete your registration:</p>"
        + "<div style=\"background-color: #f1f5f9; padding: 18px; text-align: center; border-radius: 6px; margin: 24px 0;\">"
        + "<span style=\"font-size: 32px; font-weight: 700; letter-spacing: 6px; color: #2563eb;\">" + otp + "</span>"
        + "</div>"
        + "<p style=\"color: #64748b; font-size: 13px; line-height: 1.5;\">This code is valid for 5 minutes. If you did not request this code, you can safely ignore this email.</p>"
        + "<hr style=\"border: none; border-top: 1px solid #e2e8f0; margin: 24px 0;\">"
        + "<p style=\"color: #94a3b8; font-size: 12px; margin-bottom: 0; text-align: center;\">&copy; Rivo. All rights reserved.</p>"
        + "</div>";
  }
}
