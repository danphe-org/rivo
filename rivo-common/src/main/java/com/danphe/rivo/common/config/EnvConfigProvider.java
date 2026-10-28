package com.danphe.rivo.common.config;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EnvConfigProvider {

  private static final long CHECK_INTERVAL_MS = 30_000;

  private final Path envFilePath;
  private final AtomicReference<ConcurrentHashMap<String, String>> properties =
      new AtomicReference<>(new ConcurrentHashMap<>());
  private final AtomicLong lastCheckedAt = new AtomicLong(0);
  private volatile long lastModifiedTime = 0;

  public EnvConfigProvider(@Value("${env.file.path:#{null}}") String envPath) {
    this.envFilePath = resolveEnvFilePath(envPath);
  }

  @PostConstruct
  void init() {
    loadProperties();
  }

  public static Path resolveEnvFilePath(String configuredPath) {
    if (configuredPath != null && !configuredPath.isBlank()) {
      Path p = Paths.get(configuredPath);
      if (Files.exists(p)) {
        return p.toAbsolutePath().normalize();
      }
    }

    String sysProp = System.getProperty("ENV_FILE_PATH", System.getenv("ENV_FILE_PATH"));
    if (sysProp != null && !sysProp.isBlank()) {
      Path p = Paths.get(sysProp);
      if (Files.exists(p)) {
        return p.toAbsolutePath().normalize();
      }
    }

    Path current = Paths.get(".env").toAbsolutePath().normalize();
    if (Files.exists(current)) {
      return current;
    }

    Path dir = Paths.get("").toAbsolutePath().normalize();
    for (int i = 0; i < 5 && dir != null; i++) {
      Path candidate = dir.resolve(".env");
      if (Files.exists(candidate)) {
        return candidate.normalize();
      }
      dir = dir.getParent();
    }

    return Paths.get(".env").toAbsolutePath().normalize();
  }

  public String get(String key) {
    reloadIfDue();
    String val = lookup(key);
    if (val != null) {
      return val;
    }
    String sysEnv = System.getenv(key);
    if (sysEnv != null && !sysEnv.isBlank()) {
      return sysEnv.trim();
    }
    String sysProp = System.getProperty(key);
    return (sysProp != null && !sysProp.isBlank()) ? sysProp.trim() : null;
  }

  public String get(String key, String defaultValue) {
    String val = get(key);
    return val != null ? val : defaultValue;
  }

  public String require(String key) {
    String val = get(key);
    if (val == null || val.isBlank()) {
      throw new IllegalStateException("Required configuration key '" + key + "' is missing from .env");
    }
    return val;
  }

  public int getInt(String key, int defaultValue) {
    String val = get(key);
    if (val == null || val.isBlank()) {
      return defaultValue;
    }
    try {
      return Integer.parseInt(val.trim());
    } catch (NumberFormatException e) {
      log.warn("Invalid integer value for key '{}': '{}', using default {}", key, val, defaultValue);
      return defaultValue;
    }
  }

  public long getLong(String key, long defaultValue) {
    String val = get(key);
    if (val == null || val.isBlank()) {
      return defaultValue;
    }
    try {
      return Long.parseLong(val.trim());
    } catch (NumberFormatException e) {
      log.warn("Invalid long value for key '{}': '{}', using default {}", key, val, defaultValue);
      return defaultValue;
    }
  }

  public boolean getBoolean(String key, boolean defaultValue) {
    String val = get(key);
    if (val == null || val.isBlank()) {
      return defaultValue;
    }
    return Boolean.parseBoolean(val.trim());
  }

  public boolean isFilePresent() {
    return Files.exists(envFilePath);
  }

  public int getLoadedPropertyCount() {
    return properties.get().size();
  }

  public Path getEnvFilePath() {
    return envFilePath;
  }

  public Map<String, String> getAllProperties() {
    return Collections.unmodifiableMap(properties.get());
  }

  private String lookup(String key) {
    if (key == null) return null;
    Map<String, String> map = properties.get();
    String val = map.get(key);
    if (val != null) return val.trim();

    String alias1 = key.toUpperCase().replace('.', '_');
    val = map.get(alias1);
    if (val != null) return val.trim();

    String alias2 = key.toLowerCase().replace('_', '.');
    val = map.get(alias2);
    return val != null ? val.trim() : null;
  }

  private void reloadIfDue() {
    long now = System.currentTimeMillis();
    long last = lastCheckedAt.get();
    if (now - last < CHECK_INTERVAL_MS) {
      return;
    }
    if (!lastCheckedAt.compareAndSet(last, now)) {
      return;
    }
    try {
      if (Files.exists(envFilePath)) {
        long currentModified = Files.getLastModifiedTime(envFilePath).toMillis();
        if (currentModified > lastModifiedTime) {
          log.info(".env file changed on disk, reloading...");
          loadProperties();
        }
      }
    } catch (IOException e) {
      log.warn("Failed to check .env last modified time: {}", e.getMessage());
    }
  }

  private synchronized void loadProperties() {
    if (!Files.exists(envFilePath)) {
      log.warn("No .env file found at {}", envFilePath);
      return;
    }

    ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
    try (BufferedReader reader = Files.newBufferedReader(envFilePath, StandardCharsets.UTF_8)) {
      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty() || line.startsWith("#")) {
          continue;
        }
        int eq = line.indexOf('=');
        if (eq <= 0) {
          continue;
        }
        String key = line.substring(0, eq).trim();
        String val = line.substring(eq + 1).trim();

        if ((val.startsWith("\"") && val.endsWith("\""))
            || (val.startsWith("'") && val.endsWith("'"))) {
          val = val.substring(1, val.length() - 1);
        }

        map.put(key, val);
        map.put(key.toUpperCase().replace('.', '_'), val);
        map.put(key.toLowerCase().replace('_', '.'), val);
      }

      this.properties.set(map);
      this.lastModifiedTime = Files.getLastModifiedTime(envFilePath).toMillis();
      log.info("Successfully loaded {} properties from .env file: {}", map.size(), envFilePath);
    } catch (IOException e) {
      log.error("Failed to parse .env file: {}", envFilePath, e);
    }
  }
}
