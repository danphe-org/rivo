package com.danphe.rivo.web.system.masterdataitem;

import com.danphe.rivo.common.library.base.common.Page;
import com.danphe.rivo.common.library.base.rest.RestResponse;
import com.danphe.rivo.common.masterdataitem.MasterDataItemDomain;
import com.danphe.rivo.common.masterdataitem.MasterDataItemFilter;
import com.danphe.rivo.common.user.UserDomain;
import com.danphe.rivo.service.api.MasterDataItemService;
import com.danphe.rivo.web.system.security.SystemUserPrincipal;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/api/system/master-data-items")
@PreAuthorize("hasAnyRole('SYSTEM_USER', 'ADMIN', 'STAFF') or hasAuthority('SYSTEM_USER')")
public class MasterDataItemController {

  private final MasterDataItemService service;

  public MasterDataItemController(MasterDataItemService service) {
    this.service = service;
  }

  @PostMapping
  public ResponseEntity<?> create(
      @Valid @RequestBody MasterDataItemCreateDto request,
      @AuthenticationPrincipal SystemUserPrincipal principal) {
    UserDomain actor = principal != null ? principal.getUser() : null;
    MasterDataItemDomain created = service.insert(request.toDomain(), actor);
    return RestResponse.ok(MasterDataItemResponseDto.from(created));
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getById(
      @PathVariable String id,
      @AuthenticationPrincipal SystemUserPrincipal principal) {
    UserDomain actor = principal != null ? principal.getUser() : null;
    MasterDataItemDomain domain = service.getById(id, actor);
    return RestResponse.ok(MasterDataItemResponseDto.from(domain));
  }

  @GetMapping("/master-data/{masterDataId}")
  public ResponseEntity<?> getByMasterDataId(
      @PathVariable String masterDataId,
      @AuthenticationPrincipal SystemUserPrincipal principal) {
    UserDomain actor = principal != null ? principal.getUser() : null;
    List<MasterDataItemDomain> list = service.getByMasterDataId(masterDataId, actor);
    return RestResponse.ok(list.stream().map(MasterDataItemResponseDto::from).toList());
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> update(
      @PathVariable String id,
      @Valid @RequestBody MasterDataItemUpdateDto request,
      @AuthenticationPrincipal SystemUserPrincipal principal) {
    UserDomain actor = principal != null ? principal.getUser() : null;
    MasterDataItemDomain updated = service.update(request.toDomain(id), actor);
    return RestResponse.ok(MasterDataItemResponseDto.from(updated));
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
      @RequestBody(required = false) MasterDataItemFilter filter,
      @AuthenticationPrincipal SystemUserPrincipal principal) {
    if (filter == null) {
      filter = new MasterDataItemFilter();
    }
    UserDomain actor = principal != null ? principal.getUser() : null;
    Page<MasterDataItemDomain> page = service.search(filter, actor);
    return RestResponse.ok(page.map(MasterDataItemResponseDto::from));
  }
}
