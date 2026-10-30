package com.danphe.rivo.web.client.user;

import com.danphe.rivo.common.library.base.rest.RestResponse;
import com.danphe.rivo.common.user.UserDomain;
import com.danphe.rivo.service.api.UserService;
import com.danphe.rivo.web.client.security.ClientUserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/client/users")
public class UserController {

  private final UserService userService;

  public UserController(@org.springframework.beans.factory.annotation.Qualifier("clientUserService") UserService userService) {
    this.userService = userService;
  }

  @PostMapping("/register")
  public ResponseEntity<?> register(@RequestBody UserRequest request) {
    UserDomain created = userService.register(request.toDomain());
    return RestResponse.ok(UserResponse.from(created));
  }

  @GetMapping("/me")
  public ResponseEntity<?> getMe(@AuthenticationPrincipal ClientUserPrincipal principal) {
    UserDomain actor = principal != null ? principal.getUser() : null;
    String userId = actor != null ? actor.getId() : null;
    UserDomain user = userService.getById(userId, actor);
    return RestResponse.ok(UserResponse.from(user));
  }

  @GetMapping("/{userId}")
  public ResponseEntity<?> getById(
      @PathVariable String userId,
      @AuthenticationPrincipal ClientUserPrincipal principal) {
    UserDomain actor = principal != null ? principal.getUser() : null;
    UserDomain user = userService.getById(userId, actor);
    return RestResponse.ok(UserResponse.from(user));
  }

  @PutMapping("/{userId}")
  public ResponseEntity<?> updateById(
      @PathVariable String userId,
      @RequestBody UserRequest request,
      @AuthenticationPrincipal ClientUserPrincipal principal) {
    UserDomain actor = principal != null ? principal.getUser() : null;
    UserDomain domain = request.toDomain();
    domain.setId(userId);
    UserDomain updated = userService.update(domain, actor);
    return RestResponse.ok(UserResponse.from(updated));
  }
}
