package com.danphe.rivo.web.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.danphe.rivo")
public class RivoClientApplication {

  public static void main(String[] args) {
    SpringApplication.run(RivoClientApplication.class, args);
  }
}
