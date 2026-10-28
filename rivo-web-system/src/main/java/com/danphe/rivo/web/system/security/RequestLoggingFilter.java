package com.danphe.rivo.web.system.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RequestLoggingFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String uri = request.getRequestURI();
    return uri.startsWith("/actuator") || uri.startsWith("/error");
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {

    long start = System.currentTimeMillis();
    try {
      chain.doFilter(request, response);
    } finally {
      long ms = System.currentTimeMillis() - start;
      int status = response.getStatus();
      String user = MDC.get("user");
      String ip = MDC.get("ip");
      String userPart = user != null ? " [" + user + "]" : "";
      String ipPart = ip != null ? " [" + ip + "]" : "";
      String line = request.getMethod() + " " + request.getRequestURI()
          + " -> " + status + " (" + ms + "ms)" + ipPart + userPart;

      if (status >= 500) {
        log.error(line);
      } else if (status >= 400) {
        log.warn(line);
      } else {
        log.info(line);
      }
    }
  }
}
