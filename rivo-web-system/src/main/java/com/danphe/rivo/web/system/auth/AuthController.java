package com.danphe.rivo.web.system.auth;

import com.danphe.rivo.common.library.base.exception.ForbiddenException;
import com.danphe.rivo.common.library.base.exception.UnauthorizedException;
import com.danphe.rivo.common.library.base.rest.RestResponse;
import com.danphe.rivo.common.user.UserDomain;
import com.danphe.rivo.service.api.UserService;
import com.danphe.rivo.web.system.security.JwtTokenProvider;
import com.danphe.rivo.web.system.security.MdcSetupFilter;
import com.danphe.rivo.web.system.security.SystemUserPrincipal;
import com.danphe.rivo.web.system.user.UserResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system/auth")
public class AuthController {

  private final UserService userService;
  private final JwtTokenProvider jwtProvider;
  private final PasswordEncoder passwordEncoder;
  private final MdcSetupFilter mdcSetupFilter;

  public AuthController(
      UserService userService,
      JwtTokenProvider jwtProvider,
      PasswordEncoder passwordEncoder,
      MdcSetupFilter mdcSetupFilter) {
    this.userService = userService;
    this.jwtProvider = jwtProvider;
    this.passwordEncoder = passwordEncoder;
    this.mdcSetupFilter = mdcSetupFilter;
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
    UserDomain user = userService.findByEmail(request.email())
        .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

    if (user.getPassword() == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
      throw new UnauthorizedException("Invalid email or password");
    }

    if (!user.isActive()) {
      throw new UnauthorizedException("User account is inactive");
    }

    if (!user.isSystemUser()) {
      throw new ForbiddenException("Access denied: Administrative privileges required");
    }

    String accessToken = jwtProvider.generateAccessToken(user.getId());
    String refreshToken = jwtProvider.generateRefreshToken(user.getId());

    response.addHeader(HttpHeaders.SET_COOKIE, jwtProvider.accessTokenCookie(accessToken).toString());
    response.addHeader(HttpHeaders.SET_COOKIE, jwtProvider.refreshTokenCookie(refreshToken).toString());

    return RestResponse.ok(UserResponse.withTokens(user, accessToken, refreshToken));
  }

  @PostMapping("/refresh")
  public ResponseEntity<?> refresh(
      @RequestBody(required = false) Map<String, String> body,
      HttpServletRequest request,
      HttpServletResponse response) {
    String refreshToken = null;
    if (body != null && body.containsKey("refresh_token")) {
      refreshToken = body.get("refresh_token");
    }
    if (refreshToken == null || refreshToken.isBlank()) {
      refreshToken = extractCookie(request, JwtTokenProvider.REFRESH_TOKEN_COOKIE)
          .orElseThrow(() -> new UnauthorizedException("Refresh token missing"));
    }

    if (!jwtProvider.isValid(refreshToken)) {
      throw new UnauthorizedException("Invalid refresh token");
    }

    String userId = jwtProvider.extractUserId(refreshToken);
    String newAccess = jwtProvider.generateAccessToken(userId);
    String newRefresh = jwtProvider.generateRefreshToken(userId);

    response.addHeader(HttpHeaders.SET_COOKIE, jwtProvider.accessTokenCookie(newAccess).toString());
    response.addHeader(HttpHeaders.SET_COOKIE, jwtProvider.refreshTokenCookie(newRefresh).toString());

    return RestResponse.ok(Map.of(
        "access_token", newAccess,
        "refresh_token", newRefresh,
        "token_type", "Bearer"
    ));
  }

  @PostMapping("/logout")
  public ResponseEntity<?> logout(HttpServletResponse response) {
    response.addHeader(HttpHeaders.SET_COOKIE, jwtProvider.clearAccessTokenCookie().toString());
    response.addHeader(HttpHeaders.SET_COOKIE, jwtProvider.clearRefreshTokenCookie().toString());
    response.addHeader(HttpHeaders.SET_COOKIE, mdcSetupFilter.clearSessionCookie().toString());
    return RestResponse.ok(Map.of("message", "Logged out successfully"));
  }

  @GetMapping("/me")
  public ResponseEntity<?> me() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !(auth.getPrincipal() instanceof SystemUserPrincipal principal)) {
      throw new UnauthorizedException("Not authenticated");
    }
    return RestResponse.ok(UserResponse.from(principal.getUser()));
  }

  private Optional<String> extractCookie(HttpServletRequest request, String name) {
    if (request.getCookies() == null) {
      return Optional.empty();
    }
    return Arrays.stream(request.getCookies())
        .filter(c -> name.equals(c.getName()))
        .map(Cookie::getValue)
        .findFirst();
  }
}
