package com.danphe.rivo.web.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.danphe.rivo")
public class RivoSystemApplication {

  public static void main(String[] args) {
    SpringApplication.run(RivoSystemApplication.class, args);
  }
}
