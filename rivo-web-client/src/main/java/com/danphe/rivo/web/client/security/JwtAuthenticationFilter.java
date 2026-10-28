package com.danphe.rivo.web.client.security;

import com.danphe.rivo.common.context.ApplicationRequestContext;
import com.danphe.rivo.common.context.RequestContextHolder;
import com.danphe.rivo.common.modules.ApiRequestResourceType;
import com.danphe.rivo.service.api.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtProvider;
  private final UserService userService;

  public JwtAuthenticationFilter(JwtTokenProvider jwtProvider, UserService userService) {
    this.jwtProvider = jwtProvider;
    this.userService = userService;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {

    resolveToken(request)
        .filter(jwtProvider::isValid)
        .ifPresent(token -> {
          String userId = jwtProvider.extractUserId(token);
          if (SecurityContextHolder.getContext().getAuthentication() == null) {
            userService.findById(userId).ifPresent(user -> {
              ClientUserPrincipal principal = new ClientUserPrincipal(user);
              UsernamePasswordAuthenticationToken auth =
                  new UsernamePasswordAuthenticationToken(
                      principal, null, principal.getAuthorities());
              auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
              SecurityContextHolder.getContext().setAuthentication(auth);

              ApplicationRequestContext ctx = new ApplicationRequestContext();
              ctx.setId(user.getId());
              ctx.setSubject(user.getEmail());
              ctx.setIp(MdcSetupFilter.resolveClientIp(request));
              ctx.setSessionId(MDC.get("sessionId"));
              ctx.setAgent(request.getHeader("User-Agent"));
              ctx.setResource(ApiRequestResourceType.WEB_CLIENT.getValue());
              RequestContextHolder.set(ctx);

              MDC.put("user", user.getId());
            });
          }
        });
    chain.doFilter(request, response);
  }

  private Optional<String> resolveToken(HttpServletRequest request) {
    Optional<String> fromCookie = extractTokenFromCookie(request, JwtTokenProvider.ACCESS_TOKEN_COOKIE);
    if (fromCookie.isPresent()) {
      return fromCookie;
    }
    String header = request.getHeader("Authorization");
    if (header != null && header.startsWith("Bearer ")) {
      return Optional.of(header.substring(7));
    }
    return Optional.empty();
  }

  private Optional<String> extractTokenFromCookie(HttpServletRequest request, String cookieName) {
    if (request.getCookies() == null) {
      return Optional.empty();
    }
    return Arrays.stream(request.getCookies())
        .filter(c -> cookieName.equals(c.getName()))
        .map(Cookie::getValue)
        .findFirst();
  }
}
