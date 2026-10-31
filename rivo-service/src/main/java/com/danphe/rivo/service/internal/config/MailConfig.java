package com.danphe.rivo.service.internal.config;

import com.danphe.rivo.common.config.AppConfig;
import java.util.Properties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfig {

  private final AppConfig appConfig;

  public MailConfig(AppConfig appConfig) {
    this.appConfig = appConfig;
  }

  @Bean
  public JavaMailSender javaMailSender() {
    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    mailSender.setHost(appConfig.getEmailHost());
    mailSender.setPort(appConfig.getEmailPort());
    mailSender.setUsername(appConfig.getEmailUsername());
    mailSender.setPassword(appConfig.getEmailPassword());

    Properties props = mailSender.getJavaMailProperties();
    props.put("mail.transport.protocol", "smtp");
    boolean hasAuth = appConfig.getEmailUsername() != null && !appConfig.getEmailUsername().isBlank();
    props.put("mail.smtp.auth", hasAuth ? "true" : "false");
    props.put("mail.smtp.starttls.enable", appConfig.isEmailUseTls() ? "true" : "false");
    props.put("mail.smtp.connectiontimeout", "10000");
    props.put("mail.smtp.timeout", "10000");
    props.put("mail.smtp.writetimeout", "10000");
    return mailSender;
  }
}
