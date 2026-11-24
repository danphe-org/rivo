package com.danphe.rivo.common.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

  public static final String PROPERTY_SOURCE_NAME = "rivoDotenv";
  private final Log log;

  public DotenvEnvironmentPostProcessor(DeferredLogFactory logFactory) {
    this.log = logFactory.getLog(DotenvEnvironmentPostProcessor.class);
  }

  public DotenvEnvironmentPostProcessor() {
    this.log = LogFactory.getLog(DotenvEnvironmentPostProcessor.class);
  }

  @Override
  public void postProcessEnvironment(
      ConfigurableEnvironment environment, SpringApplication application) {
    Path envPath = EnvConfigProvider.resolveEnvFilePath(null);
    if (!Files.exists(envPath)) {
      if (log.isDebugEnabled()) {
        log.debug(".env file not found at: " + envPath);
      }
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

        if (val.length() >= 2
            && ((val.startsWith("\"") && val.endsWith("\""))
                || (val.startsWith("'") && val.endsWith("'")))) {
          val = val.substring(1, val.length() - 1);
        }

        props.put(key, val);
        props.putIfAbsent(key.toLowerCase().replace('_', '.'), val);
      }

      if (!props.isEmpty()) {
        if (environment.getPropertySources().contains(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME)) {
          environment
              .getPropertySources()
              .addAfter(
                  StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                  new MapPropertySource(PROPERTY_SOURCE_NAME, props));
        } else {
          environment
              .getPropertySources()
              .addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, props));
        }
        if (log.isInfoEnabled()) {
          log.info("Loaded " + (props.size() / 2) + " properties from " + envPath);
        }
      }
    } catch (IOException e) {
      if (log.isErrorEnabled()) {
        log.error("Failed to read .env file at " + envPath + ": " + e.getMessage());
      }
    }
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }
}
