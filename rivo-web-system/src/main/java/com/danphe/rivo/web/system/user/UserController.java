package com.danphe.rivo.web.system.user;

import com.danphe.rivo.common.library.base.common.Page;
import com.danphe.rivo.common.library.base.rest.RestResponse;
import com.danphe.rivo.common.user.UserDomain;
import com.danphe.rivo.common.user.UserFilter;
import com.danphe.rivo.service.api.UserService;
import com.danphe.rivo.web.system.security.SystemUserPrincipal;
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
@RequestMapping("/api/system/users")
@PreAuthorize("hasAnyRole('SYSTEM_USER', 'ADMIN', 'STAFF') or hasAuthority('SYSTEM_USER')")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping
  public ResponseEntity<?> create(
      @RequestBody UserRequest request,
      @AuthenticationPrincipal SystemUserPrincipal principal) {
    UserDomain actor = principal != null ? principal.getUser() : null;
    UserDomain created = userService.insert(request.toDomain(), actor);
    return RestResponse.ok(UserResponse.from(created));
  }

  @GetMapping("/{userId}")
  public ResponseEntity<?> getById(
      @PathVariable String userId,
      @AuthenticationPrincipal SystemUserPrincipal principal) {
    UserDomain actor = principal != null ? principal.getUser() : null;
    UserDomain user = userService.getById(userId, actor);
    return RestResponse.ok(UserResponse.from(user));
  }

  @PutMapping("/{userId}")
  public ResponseEntity<?> update(
      @PathVariable String userId,
      @RequestBody UserRequest request,
      @AuthenticationPrincipal SystemUserPrincipal principal) {
    UserDomain actor = principal != null ? principal.getUser() : null;
    UserDomain domain = request.toDomain();
    domain.setId(userId);
    UserDomain updated = userService.update(domain, actor);
    return RestResponse.ok(UserResponse.from(updated));
  }

  @DeleteMapping("/{userId}")
  public ResponseEntity<?> delete(
      @PathVariable String userId,
      @AuthenticationPrincipal SystemUserPrincipal principal) {
    UserDomain actor = principal != null ? principal.getUser() : null;
    userService.delete(userId, actor);
    return RestResponse.ok(null);
  }

  @PostMapping("/search")
  public ResponseEntity<?> search(
      @RequestBody(required = false) UserFilter filter,
      @AuthenticationPrincipal SystemUserPrincipal principal) {
    if (filter == null) {
      filter = new UserFilter();
    }
    UserDomain actor = principal != null ? principal.getUser() : null;
    Page<UserDomain> page = userService.findAll(filter, actor);
    Page<UserResponse> responsePage = page.map(UserResponse::from);
    return RestResponse.ok(responsePage);
  }

  @PostMapping("/list")
  public ResponseEntity<?> listPost(
      @RequestBody(required = false) UserFilter filter,
      @AuthenticationPrincipal SystemUserPrincipal principal) {
    return search(filter, principal);
  }

  @GetMapping
  public ResponseEntity<?> listGet(
      UserFilter filter,
      @AuthenticationPrincipal SystemUserPrincipal principal) {
    return search(filter, principal);
  }
}
