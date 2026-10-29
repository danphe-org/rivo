package com.danphe.rivo.web.client.matchrequest;

import com.danphe.rivo.common.matchrequest.MatchRequestDomain;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record MatchRequestCreateDto(
    @NotBlank(message = "Game is required")
    @Size(max = 100, message = "Game name cannot exceed 100 characters")
    String game,

    @NotNull(message = "Request type is required")
    @NotBlank(message = "Request type cannot be blank")
    @Pattern(regexp = "(?i)^(OPPONENT|TEAMMATE)$", message = "Request type must be either OPPONENT or TEAMMATE")
    @JsonProperty("request_type")
    String requestType,

    @Size(max = 100, message = "Category cannot exceed 100 characters")
    String category,

    @Size(max = 2000, message = "Message cannot exceed 2000 characters")
    String message,

    LocalDateTime time,

    Boolean urgent
) {
  public MatchRequestDomain toDomain() {
    MatchRequestDomain domain = new MatchRequestDomain();
    domain.setGame(game);
    domain.setRequestType(requestType);
    domain.setCategory(category);
    domain.setMessage(message);
    domain.setTime(time);
    domain.setUrgent(Boolean.TRUE.equals(urgent));
    return domain;
  }
}
