package com.danphe.rivo.web.client.matchrequest;

import com.danphe.rivo.common.library.base.common.Page;
import com.danphe.rivo.common.library.base.rest.RestResponse;
import com.danphe.rivo.common.matchrequest.MatchRequestDomain;
import com.danphe.rivo.common.matchrequest.MatchRequestFilter;
import com.danphe.rivo.common.user.UserDomain;
import com.danphe.rivo.service.api.MatchRequestService;
import com.danphe.rivo.web.client.security.ClientUserPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/client/match-requests")
public class MatchRequestController {

  private final MatchRequestService service;

  public MatchRequestController(MatchRequestService service) {
    this.service = service;
  }

  @PostMapping
  public ResponseEntity<?> create(
      @Valid @RequestBody MatchRequestCreateDto request,
      @AuthenticationPrincipal ClientUserPrincipal principal) {
    UserDomain actor = principal != null ? principal.getUser() : null;
    MatchRequestDomain created = service.insert(request.toDomain(), actor);
    return RestResponse.ok(MatchRequestResponseDto.from(created));
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getById(
      @PathVariable String id,
      @AuthenticationPrincipal ClientUserPrincipal principal) {
    UserDomain actor = principal != null ? principal.getUser() : null;
    MatchRequestDomain domain = service.getById(id, actor);
    return RestResponse.ok(MatchRequestResponseDto.from(domain));
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> update(
      @PathVariable String id,
      @Valid @RequestBody MatchRequestUpdateDto request,
      @AuthenticationPrincipal ClientUserPrincipal principal) {
    UserDomain actor = principal != null ? principal.getUser() : null;
    MatchRequestDomain updated = service.update(request.toDomain(id), actor);
    return RestResponse.ok(MatchRequestResponseDto.from(updated));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> delete(
      @PathVariable String id,
      @AuthenticationPrincipal ClientUserPrincipal principal) {
    UserDomain actor = principal != null ? principal.getUser() : null;
    service.delete(id, actor);
    return RestResponse.ok(null);
  }

  @PostMapping("/search")
  public ResponseEntity<?> search(
      @RequestBody(required = false) MatchRequestFilter filter,
      @AuthenticationPrincipal ClientUserPrincipal principal) {
    if (filter == null) {
      filter = new MatchRequestFilter();
    }
    UserDomain actor = principal != null ? principal.getUser() : null;
    Page<MatchRequestDomain> page = service.search(filter, actor);
    return RestResponse.ok(page.map(MatchRequestResponseDto::from));
  }
}
