package com.danphe.rivo.web.system.auth;

import com.danphe.rivo.common.library.base.rest.RestResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system/ping")
public class PingController {

  @GetMapping
  public ResponseEntity<?> ping() {
    return RestResponse.ok("pong");
  }
}
