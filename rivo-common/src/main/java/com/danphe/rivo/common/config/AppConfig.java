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
}
