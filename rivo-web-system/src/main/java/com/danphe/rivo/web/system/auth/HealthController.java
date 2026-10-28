package com.danphe.rivo.web.system.auth;

import com.danphe.rivo.common.library.base.rest.RestResponse;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

  @GetMapping("/health")
  public ResponseEntity<?> health() {
    return RestResponse.ok(Map.of(
        "status", "healthy",
        "environment", "development",
        "database", "connected"
    ));
  }
}
