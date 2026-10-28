package com.danphe.rivo.web.system.security;

import com.danphe.rivo.common.config.EnvConfigProvider;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

  public static final String ACCESS_TOKEN_COOKIE = "access_token";
  public static final String REFRESH_TOKEN_COOKIE = "refresh_token";

  private final EnvConfigProvider envConfig;

  @Value("${jwt.access-token.expiry-ms:900000}")
  private long accessTokenExpiryMs;

  @Value("${jwt.refresh-token.expiry-ms:604800000}")
  private long refreshTokenExpiryMs;

  @Value("${jwt.cookie.secure:false}")
  private boolean cookieSecure;

  public JwtTokenProvider(EnvConfigProvider envConfig) {
    this.envConfig = envConfig;
  }

  public String generateAccessToken(String userId) {
    return buildToken(userId, accessTokenExpiryMs);
  }

  public String generateRefreshToken(String userId) {
    return buildToken(userId, refreshTokenExpiryMs);
  }

  public String extractUserId(String token) {
    return Jwts.parser()
        .verifyWith(signingKey())
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .getSubject();
  }

  public boolean isValid(String token) {
    try {
      Jwts.parser().verifyWith(signingKey()).build().parseSignedClaims(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  public ResponseCookie accessTokenCookie(String token) {
    return cookie(ACCESS_TOKEN_COOKIE, token, Duration.ofMillis(accessTokenExpiryMs));
  }

  public ResponseCookie refreshTokenCookie(String token) {
    return cookie(REFRESH_TOKEN_COOKIE, token, Duration.ofMillis(refreshTokenExpiryMs));
  }

  public ResponseCookie clearAccessTokenCookie() {
    return cookie(ACCESS_TOKEN_COOKIE, "", Duration.ZERO);
  }

  public ResponseCookie clearRefreshTokenCookie() {
    return cookie(REFRESH_TOKEN_COOKIE, "", Duration.ZERO);
  }

  private String buildToken(String userId, long expiryMs) {
    return Jwts.builder()
        .subject(userId)
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + expiryMs))
        .signWith(signingKey())
        .compact();
  }

  private ResponseCookie cookie(String name, String value, Duration maxAge) {
    return ResponseCookie.from(name, value)
        .httpOnly(true)
        .secure(cookieSecure)
        .path("/")
        .maxAge(maxAge)
        .sameSite(cookieSecure ? "None" : "Lax")
        .build();
  }

  private SecretKey signingKey() {
    String secret = envConfig.get("JWT_SECRET");
    if (secret == null || secret.isBlank()) {
      throw new IllegalStateException("Required config property 'JWT_SECRET' is missing from .env");
    }
    try {
      return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    } catch (Exception e) {
      return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
  }
}
