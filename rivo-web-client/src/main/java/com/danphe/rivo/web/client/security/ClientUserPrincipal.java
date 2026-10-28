package com.danphe.rivo.web.client.security;

import com.danphe.rivo.common.user.UserDomain;
import com.danphe.rivo.common.user.UserRoleDomain;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Data
public class ClientUserPrincipal implements UserDetails {

  private final UserDomain user;

  public ClientUserPrincipal(UserDomain user) {
    this.user = user;
  }

  @Override
  public String getUsername() {
    return user.getEmail();
  }

  @Override
  public String getPassword() {
    return user.getPassword();
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    List<GrantedAuthority> authorities = new ArrayList<>();
    authorities.add(new SimpleGrantedAuthority("ROLE_CLIENT"));
    authorities.add(new SimpleGrantedAuthority("CLIENT"));
    if (user.getRole() != null) {
      authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase()));
      authorities.add(new SimpleGrantedAuthority(user.getRole().toUpperCase()));
    }
    if (user.getRoles() != null) {
      for (UserRoleDomain r : user.getRoles()) {
        if (!r.isDeleted() && r.isActive() && r.getRole() != null) {
          authorities.add(new SimpleGrantedAuthority("ROLE_" + r.getRole().toUpperCase()));
          authorities.add(new SimpleGrantedAuthority(r.getRole().toUpperCase()));
        }
      }
    }
    return authorities;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return user.isActive() && !user.isDeleted();
  }
}
