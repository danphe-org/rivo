package com.danphe.rivo.web.system.matchrequest;

import com.danphe.rivo.common.matchrequest.MatchRequestDomain;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record MatchRequestUpdateDto(
    @Size(max = 100, message = "Game name cannot exceed 100 characters")
    String game,

    @Pattern(regexp = "(?i)^(OPPONENT|TEAMMATE)$", message = "Request type must be either OPPONENT or TEAMMATE")
    @JsonProperty("request_type")
    String requestType,

    @Pattern(regexp = "(?i)^(OPEN|MATCHED|FILLED|CANCELLED|COMPLETED)$", message = "Status must be OPEN, MATCHED, FILLED, CANCELLED, or COMPLETED")
    String status,

    @Size(max = 100, message = "Category cannot exceed 100 characters")
    String category,

    @Size(max = 2000, message = "Message cannot exceed 2000 characters")
    String message,

    LocalDateTime time,

    Boolean urgent
) {
  public MatchRequestDomain toDomain(String id) {
    MatchRequestDomain domain = new MatchRequestDomain();
    domain.setId(id);
    domain.setGame(game);
    domain.setRequestType(requestType);
    domain.setStatus(status);
    domain.setCategory(category);
    domain.setMessage(message);
    domain.setTime(time);
    if (urgent != null) {
      domain.setUrgent(urgent);
    }
    return domain;
  }
}
