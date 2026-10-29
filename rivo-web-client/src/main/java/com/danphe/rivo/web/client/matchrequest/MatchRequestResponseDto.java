package com.danphe.rivo.web.client.matchrequest;

import com.danphe.rivo.common.matchrequest.MatchRequestDomain;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MatchRequestResponseDto(
    String id,
    @JsonProperty("requested_by")
    String requestedBy,
    @JsonProperty("requested_by_name")
    String requestedByName,
    @JsonProperty("requested_by_email")
    String requestedByEmail,
    @JsonProperty("request_type")
    String requestType,
    String status,
    String game,
    String category,
    String message,
    LocalDateTime time,
    boolean urgent,
    @JsonProperty("created_on")
    LocalDateTime createdOn,
    @JsonProperty("modified_on")
    LocalDateTime modifiedOn,
    @JsonProperty("created_by")
    String createdBy,
    @JsonProperty("modified_by")
    String modifiedBy,
    boolean active,
    boolean deleted
) {
  public static MatchRequestResponseDto from(MatchRequestDomain domain) {
    if (domain == null) return null;
    return new MatchRequestResponseDto(
        domain.getId(),
        domain.getRequestedBy(),
        domain.getRequestedByName(),
        domain.getRequestedByEmail(),
        domain.getRequestType(),
        domain.getStatus(),
        domain.getGame(),
        domain.getCategory(),
        domain.getMessage(),
        domain.getTime(),
        domain.isUrgent(),
        domain.getCreatedOn(),
        domain.getLastModifiedOn(),
        domain.getCreatedBy(),
        domain.getLastModifiedBy(),
        domain.isActive(),
        domain.isDeleted()
    );
  }
}
