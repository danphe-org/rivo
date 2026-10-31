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
    mailSender.setDefaultEncoding("UTF-8");

    Properties props = mailSender.getJavaMailProperties();
    props.put("mail.transport.protocol", "smtp");
    props.put("mail.smtp.auth", appConfig.isEmailAuth() ? "true" : "false");

    if (appConfig.getEmailPort() == 465) {
      props.put("mail.smtp.ssl.enable", "true");
      props.put("mail.smtp.socketFactory.port", "465");
      props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
    } else {
      props.put("mail.smtp.starttls.enable", appConfig.isEmailUseTls() ? "true" : "false");
      props.put("mail.smtp.starttls.required", "true");
    }

    props.put("mail.smtp.ssl.trust", "*");
    props.put("mail.smtp.connectiontimeout", "10000");
    props.put("mail.smtp.timeout", "10000");
    props.put("mail.smtp.writetimeout", "10000");
    props.put("mail.debug", "true");
    return mailSender;
  }
}
