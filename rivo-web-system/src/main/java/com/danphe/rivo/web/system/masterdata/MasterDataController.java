package com.danphe.rivo.web.system.masterdata;

import com.danphe.rivo.common.library.base.common.Page;
import com.danphe.rivo.common.library.base.rest.RestResponse;
import com.danphe.rivo.common.masterdata.MasterDataDomain;
import com.danphe.rivo.common.masterdata.MasterDataFilter;
import com.danphe.rivo.common.user.UserDomain;
import com.danphe.rivo.service.api.MasterDataService;
import com.danphe.rivo.web.system.security.SystemUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/system/master-data")
@PreAuthorize("hasAnyRole('SYSTEM_USER', 'ADMIN', 'STAFF') or hasAuthority('SYSTEM_USER')")
public class MasterDataController {

  private final MasterDataService service;

  public MasterDataController(MasterDataService service) {
    this.service = service;
  }

  @PostMapping
  public ResponseEntity<?> create(
      @Valid @RequestBody MasterDataCreateDto request,
      @AuthenticationPrincipal SystemUserPrincipal principal) {
    UserDomain actor = principal != null ? principal.getUser() : null;
    MasterDataDomain created = service.insert(request.toDomain(), actor);
    return RestResponse.ok(MasterDataResponseDto.from(created));
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getById(
      @PathVariable String id,
      @AuthenticationPrincipal SystemUserPrincipal principal) {
    UserDomain actor = principal != null ? principal.getUser() : null;
    MasterDataDomain domain = service.getById(id, actor);
    return RestResponse.ok(MasterDataResponseDto.from(domain));
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> update(
      @PathVariable String id,
      @Valid @RequestBody MasterDataUpdateDto request,
      @AuthenticationPrincipal SystemUserPrincipal principal) {
    UserDomain actor = principal != null ? principal.getUser() : null;
    MasterDataDomain updated = service.update(request.toDomain(id), actor);
    return RestResponse.ok(MasterDataResponseDto.from(updated));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> delete(
      @PathVariable String id,
      @AuthenticationPrincipal SystemUserPrincipal principal) {
    UserDomain actor = principal != null ? principal.getUser() : null;
    service.delete(id, actor);
    return RestResponse.ok(null);
  }

  @PostMapping("/search")
  public ResponseEntity<?> search(
      @RequestBody(required = false) MasterDataFilter filter,
      @AuthenticationPrincipal SystemUserPrincipal principal) {
    if (filter == null) {
      filter = new MasterDataFilter();
    }
    UserDomain actor = principal != null ? principal.getUser() : null;
    Page<MasterDataDomain> page = service.search(filter, actor);
    return RestResponse.ok(page.map(MasterDataResponseDto::from));
  }
}
