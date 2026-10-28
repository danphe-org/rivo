package com.danphe.rivo.common.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

  private static final Logger log = LoggerFactory.getLogger(DotenvEnvironmentPostProcessor.class);
  public static final String PROPERTY_SOURCE_NAME = "rivoDotenv";

  @Override
  public void postProcessEnvironment(
      ConfigurableEnvironment environment, SpringApplication application) {
    Path envPath = EnvConfigProvider.resolveEnvFilePath(null);
    if (!Files.exists(envPath)) {
      log.info(".env file not found at: {}", envPath);
      return;
    }

    Map<String, Object> props = new HashMap<>();
    try (BufferedReader reader = Files.newBufferedReader(envPath, StandardCharsets.UTF_8)) {
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

        props.put(key, val);
        props.putIfAbsent(key.toLowerCase().replace('_', '.'), val);
      }

      if (!props.isEmpty()) {
        environment
            .getPropertySources()
            .addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, props));
        log.info("Loaded {} properties from {}", props.size() / 2, envPath);
      }
    } catch (IOException e) {
      log.error("Failed to read .env file at {}: {}", envPath, e.getMessage());
    }
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE + 10;
  }
}
