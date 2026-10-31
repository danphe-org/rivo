package com.danphe.rivo.common.config;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@Component
public class AppConfig {

  private static AppConfig instance;

  private final EnvConfigProvider env;

  public AppConfig(EnvConfigProvider env) {
    this.env = env;
    AppConfig.instance = this;
  }

  public static AppConfig get() {
    return instance;
  }

  public String getProperty(String key) {
    return env.get(key);
  }

  public String getProperty(String key, String defaultValue) {
    return env.get(key, defaultValue);
  }

  public String requireProperty(String key) {
    return env.require(key);
  }

  public int getInt(String key, int defaultValue) {
    return env.getInt(key, defaultValue);
  }

  public long getLong(String key, long defaultValue) {
    return env.getLong(key, defaultValue);
  }

  public boolean getBoolean(String key, boolean defaultValue) {
    return env.getBoolean(key, defaultValue);
  }

  public String getAppName() {
    return env.get("APP_NAME", "rivo");
  }

  public String getAppEnv() {
    return env.get("APP_ENV", "development");
  }

  public boolean isProduction() {
    return "production".equalsIgnoreCase(getAppEnv());
  }

  public boolean isDevelopment() {
    return "development".equalsIgnoreCase(getAppEnv());
  }

  public String getClientFeUrl() {
    return env.get("CLIENT_FE_URL", "http://localhost:3000");
  }

  public int getServerPortClient() {
    return env.getInt("SERVER_PORT_CLIENT", 8022);
  }

  public int getServerPortSystem() {
    return env.getInt("SERVER_PORT_SYSTEM", 8021);
  }

  public String getDbUrl() {
    return env.require("DB_URL");
  }

  public String getDbUsername() {
    return env.require("DB_USERNAME");
  }

  public String getDbPassword() {
    return env.get("DB_PASSWORD", "");
  }

  public int getDbPoolMaxSize() {
    return env.getInt("DB_POOL_MAX_SIZE", 10);
  }

  public int getDbPoolMinIdle() {
    return env.getInt("DB_POOL_MIN_IDLE", 5);
  }

  public long getDbPoolConnectionTimeout() {
    return env.getLong("DB_POOL_CONNECTION_TIMEOUT", 30000L);
  }

  public long getDbPoolIdleTimeout() {
    return env.getLong("DB_POOL_IDLE_TIMEOUT", 600000L);
  }

  public long getDbPoolMaxLifetime() {
    return env.getLong("DB_POOL_MAX_LIFETIME", 1800000L);
  }

  public boolean isFlywayEnabled() {
    return env.getBoolean("FLYWAY_ENABLED", true);
  }

  public String[] getFlywaySchemas() {
    return env.get("FLYWAY_SCHEMAS", "client,system,public").split(",");
  }

  public String getFlywayDefaultSchema() {
    return env.get("FLYWAY_DEFAULT_SCHEMA", "system");
  }

  public String[] getFlywayLocations() {
    return env.get("FLYWAY_LOCATIONS", "classpath:db/migration").split(",");
  }

  public boolean isFlywayBaselineOnMigrate() {
    return env.getBoolean("FLYWAY_BASELINE_ON_MIGRATE", true);
  }

  public String getFlywayBaselineVersion() {
    return env.get("FLYWAY_BASELINE_VERSION", "0");
  }

  public String getJwtSecret() {
    return env.require("JWT_SECRET");
  }

  public long getJwtAccessTokenExpiryMs() {
    return env.getLong("JWT_ACCESS_TOKEN_EXPIRY_MS", 900000L);
  }

  public long getJwtRefreshTokenExpiryMs() {
    return env.getLong("JWT_REFRESH_TOKEN_EXPIRY_MS", 604800000L);
  }

  public boolean isJwtCookieSecure() {
    return env.getBoolean("JWT_COOKIE_SECURE", false);
  }

  public boolean isSessionCookieSecure() {
    return env.getBoolean("SESSION_COOKIE_SECURE", false);
  }

  public String getEmailHost() {
    String host = env.get("SPRING_MAIL_HOST");
    if (host == null || host.isBlank()) {
      host = env.get("EMAIL_HOST");
    }
    if (host == null || host.isBlank()) {
      host = env.get("SMTP_HOST", "");
    }
    return host;
  }

  public int getEmailPort() {
    String port = env.get("SPRING_MAIL_PORT");
    if (port == null || port.isBlank()) {
      port = env.get("EMAIL_PORT");
    }
    if (port == null || port.isBlank()) {
      port = env.get("SMTP_PORT");
    }
    if (port == null || port.isBlank()) {
      return 587;
    }
    try {
      return Integer.parseInt(port.trim());
    } catch (NumberFormatException e) {
      return 587;
    }
  }

  public String getEmailUsername() {
    String user = env.get("SPRING_MAIL_USERNAME");
    if (user == null || user.isBlank()) {
      user = env.get("EMAIL_HOST_USER");
    }
    if (user == null || user.isBlank()) {
      user = env.get("SMTP_USERNAME", "");
    }
    return user;
  }

  public String getEmailPassword() {
    String pass = env.get("SPRING_MAIL_PASSWORD");
    if (pass == null || pass.isBlank()) {
      pass = env.get("EMAIL_HOST_PASSWORD");
    }
    if (pass == null || pass.isBlank()) {
      pass = env.get("SMTP_PASSWORD", "");
    }
    return pass;
  }

  public String getEmailFrom() {
    String from = env.get("SPRING_MAIL_FROM");
    if (from == null || from.isBlank()) {
      from = env.get("DEFAULT_FROM_EMAIL");
    }
    if (from == null || from.isBlank()) {
      from = env.get("SMTP_FROM_EMAIL");
    }
    if (from == null || from.isBlank()) {
      from = getEmailUsername();
    }
    return from != null && !from.isBlank() ? from : "noreply@rivo.com";
  }

  public boolean isEmailAuth() {
    String auth = env.get("SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH");
    if (auth == null || auth.isBlank()) {
      auth = env.get("SMTP_AUTH");
    }
    if (auth != null && !auth.isBlank()) {
      return Boolean.parseBoolean(auth.trim());
    }
    String user = getEmailUsername();
    return user != null && !user.isBlank();
  }

  public boolean isEmailUseTls() {
    String tls = env.get("SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE");
    if (tls == null || tls.isBlank()) {
      tls = env.get("EMAIL_USE_TLS");
    }
    if (tls == null || tls.isBlank()) {
      tls = env.get("SMTP_STARTTLS_ENABLE");
    }
    if (tls != null && !tls.isBlank()) {
      return Boolean.parseBoolean(tls.trim());
    }
    return true;
  }

  public boolean isEmailConfigured() {
    String host = getEmailHost();
    return host != null && !host.isBlank();
  }
}
